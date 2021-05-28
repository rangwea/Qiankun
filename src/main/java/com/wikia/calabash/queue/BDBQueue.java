/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wikia.calabash.queue;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *
 * Fast queue implementation on top of Berkley DB Java Edition. This class is thread-safe.
 * <p>
 * This is based on
 * <a href="http://sysgears.com/articles/lightweight-fast-persistent-queue-in-java-using-berkley-db"></a>.
 * </p>
 *
 * Created on Jun 27, 2011
 *
 * @author Martin Grotzke (initial creation)
 */
public class BDBQueue {

    /**
     * Berkley DB environment.
     */
    private final Environment dbEnv;

    /**
     * Berkley DB instance for the queue.
     */
    private final Database queueDatabase;

    /**
     * Queue cache size - number of element operations it is allowed to loose in case of system crash.
     */
    private final int cacheSize;

    /**
     * This queue name.
     */
    private final String queueName;

    /**
     * Queue operation counter, which is used to sync the queue database to disk periodically.
     */
    private int opsCounter;

    /**
     * Creates instance of persistent queue.
     *
     * @param queueEnvPath   queue database environment directory path
     * @param queueName      descriptive queue name
     * @param cacheSize      how often to sync the queue to disk
     * @throws IOException   thrown when the given queueEnvPath does not exist and cannot be created.
     */
    public BDBQueue(final String queueEnvPath,
                 final String queueName,
                 final int cacheSize) throws IOException, DatabaseException {
        this(queueEnvPath, queueName, cacheSize, false, true);
    }

    /**
     * Creates instance of persistent queue.
     *
     * @param queueEnvPath   queue database environment directory path
     * @param queueName      descriptive queue name
     * @param cacheSize      how often to sync the queue to disk
     * @param readOnly       if the db shall be accessed readonly
     * @param allowCreate    if true, the db environment is created if it does not exist.
     * @throws IOException   thrown when the given queueEnvPath does not exist and cannot be created.
     */
    public BDBQueue(final String queueEnvPath,
                 final String queueName,
                 final int cacheSize,
                 final boolean readOnly,
                 final boolean allowCreate) throws IOException, DatabaseException {

        // Create parent dirs for queue environment directory
        mkdir(new File(queueEnvPath), allowCreate);

        // Setup database environment
        final EnvironmentConfig dbEnvConfig = new EnvironmentConfig();
        dbEnvConfig.setTransactional(false);
        dbEnvConfig.setAllowCreate(allowCreate);
        dbEnvConfig.setReadOnly(readOnly);
        this.dbEnv = new Environment(new File(queueEnvPath), dbEnvConfig);

        // Setup non-transactional deferred-write queue database
        final DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setTransactional(false);
        dbConfig.setAllowCreate(allowCreate);
        dbConfig.setReadOnly(readOnly);
        dbConfig.setDeferredWrite(true);
        dbConfig.setBtreeComparator(new KeyComparator());
        this.queueDatabase = dbEnv.openDatabase(null, queueName, dbConfig);
        this.queueName = queueName;
        this.cacheSize = cacheSize;
        this.opsCounter = 0;
    }

    /**
     * Asserts that the given directory exists and creates it if necessary.
     * @param dir the directory that shall exist
     * @param createDirectoryIfNotExisting specifies if the directory shall be created if it does not exist.
     * @throws IOException thrown if the directory could not be created.
     */
    public static void mkdir(@Nonnull final File dir, final boolean createDirectoryIfNotExisting ) throws IOException {
        // commons io FileUtils.forceMkdir would be useful here, we just want to omit this dependency
        if (!dir.exists()) {
            if(!createDirectoryIfNotExisting) {
                throw new IOException( "The directory " + dir.getAbsolutePath() + " does not exist." );
            }
            if(!dir.mkdirs()) {
                throw new IOException( "Could not create directory " + dir.getAbsolutePath() );
            }
        }
        if (!dir.isDirectory()) {
            throw new IOException("File " + dir + " exists and is not a directory. Unable to create directory.");
        }
    }

    /**
     * Retrieves and and removes element from the head of this queue.
     *
     * @return element from the head of the queue or null if queue is empty
     *
     * @throws DatabaseException in case of disk IO failure
     */
    public synchronized byte[] poll() throws DatabaseException {
        final DatabaseEntry key = new DatabaseEntry();
        final DatabaseEntry data = new DatabaseEntry();
        final Cursor cursor = queueDatabase.openCursor(null, null);
        try {
            cursor.getFirst(key, data, LockMode.RMW);
            if (data.getData() == null) {
                return null;
            }
            cursor.delete();
            opsCounter++;
            if (opsCounter >= cacheSize) {
                queueDatabase.sync();
                opsCounter = 0;
            }
            return data.getData();
        } finally {
            cursor.close();
        }
    }

    /**
     * Returns an iterator over the elements in this queue.
     *
     * @return an iterator over elemtns of the queue.
     *
     * @throws IOException in case of disk IO failure
     */
    public synchronized CloseableIterator<byte[]> iterator() throws DatabaseException {
      final DatabaseEntry key = new DatabaseEntry();
      final DatabaseEntry data = new DatabaseEntry();
      final Cursor cursor = queueDatabase.openCursor(null, null);

      return new CloseableIterator<byte[]>() {

        private byte[] nextValue;

        @Override
        public void close() {
            try {
                cursor.close();
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }

        @Override
        public boolean hasNext() {
            if (nextValue == null) {
                try {
                    final OperationStatus status = cursor.getNext(key, data, LockMode.READ_UNCOMMITTED);
                    if(status != OperationStatus.SUCCESS && status != OperationStatus.NOTFOUND) {
                        throw new IllegalStateException("Getting next element did not return successfully: " + status);
                    }
                    nextValue = status == OperationStatus.SUCCESS ? data.getData() : null;
                } catch (final DatabaseException e) {
                    throw new RuntimeException(e);
                }
                return nextValue != null;
            } else {
                return true;
            }
        }

        @Override
        public byte[] next() {
            if (hasNext()) {
                final byte[] v = nextValue;
                nextValue = null;
                return v;
            } else {
                throw new NoSuchElementException();
            }
        }

        @Override
        public void remove() {
            try {
                cursor.delete();
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }

      };
    }

    /**
     * Retrieves element from the head of this queue.
     *
     * @return element from the head of the queue or null if queue is empty
     *
     * @throws IOException in case of disk IO failure
     */
    public synchronized byte[] peek() throws DatabaseException {
      final DatabaseEntry key = new DatabaseEntry();
      final DatabaseEntry data = new DatabaseEntry();
      final Cursor cursor = queueDatabase.openCursor(null, null);
      try {
          cursor.getFirst(key, data, LockMode.RMW);
          if (data.getData() == null) {
              return null;
          }
          return data.getData();
      } finally {
          cursor.close();
      }
    }

    /**
     * Removes the eldest element.
     *
     * @throws NoSuchElementException if the queue is empty
     */
    public synchronized void remove() throws DatabaseException {
        final DatabaseEntry key = new DatabaseEntry();
        final DatabaseEntry data = new DatabaseEntry();
        final Cursor cursor = queueDatabase.openCursor(null, null);
        try {
            cursor.getFirst(key, data, LockMode.RMW);
            if (data.getData() == null) {
                throw new NoSuchElementException();
            }
            cursor.delete();
            opsCounter++;
            if (opsCounter >= cacheSize) {
                queueDatabase.sync();
                opsCounter = 0;
            }
        } finally {
            cursor.close();
        }
    }

    /**
     * Pushes element to the tail of this queue.
     *
     * @param element element
     *
     * @throws IOException in case of disk IO failure
     */
    public synchronized void push(final byte[] element) throws DatabaseException {
        final DatabaseEntry key = new DatabaseEntry();
        final DatabaseEntry data = new DatabaseEntry();
        final Cursor cursor = queueDatabase.openCursor(null, null);
        try {
            cursor.getLast(key, data, LockMode.RMW);

            BigInteger prevKeyValue;
            if (key.getData() == null) {
                prevKeyValue = BigInteger.valueOf(-1);
            } else {
                prevKeyValue = new BigInteger(key.getData());
            }
            final BigInteger newKeyValue = prevKeyValue.add(BigInteger.ONE);

            final DatabaseEntry newKey = new DatabaseEntry(
                    newKeyValue.toByteArray());
            final DatabaseEntry newData = new DatabaseEntry(element);
            queueDatabase.put(null, newKey, newData);

            opsCounter++;
            if (opsCounter >= cacheSize) {
                queueDatabase.sync();
                opsCounter = 0;
            }
        } finally {
            cursor.close();
        }
    }

    public synchronized int clear() throws DatabaseException {
        final DatabaseEntry key = new DatabaseEntry();
        final DatabaseEntry data = new DatabaseEntry();
        final Cursor cursor = queueDatabase.openCursor(null, null);
        try {
            int itemsRemoved = 0;
            while(cursor.getNext(key, data, LockMode.RMW) == OperationStatus.SUCCESS && data.getData() != null) {
                cursor.delete();
                itemsRemoved++;
            }

            queueDatabase.sync();
            opsCounter = 0;

            return itemsRemoved;
        } finally {
            cursor.close();
        }
    }

   /**
     * Returns the size of this queue.
     *
     * @return the size of the queue
     */
    public long size() throws DatabaseException {
        return queueDatabase.count();
    }

    /**
      * Determines if this queue is empty (equivalent to <code>{@link #size()} == 0</code>).
      *
      * @return <code>true</code> if this queue is empty, otherwise <code>false</code>.
      */
     public boolean isEmpty() throws DatabaseException {
         return queueDatabase.count() == 0;
     }

    /**
     * Returns this queue name.
     *
     * @return this queue name
     */
    public String getQueueName() {
        return queueName;
    }

    /**
     * Closes this queue and frees up all resources associated to it.
     */
    public void close() throws DatabaseException {
    	// When the current thread was interrupted db.close and dbEnv.close will complain:
    	// "InterruptedException may cause incorrect internal state, unable to continue. Environment is invalid and must be closed."
    	// First closing the environment also does not help, therefore we reset the interrupted state
    	// and restore it after closing if it was set
    	final boolean interrupted = Thread.interrupted();
        queueDatabase.close();
        dbEnv.close();
        if(interrupted) {
        	Thread.currentThread().interrupt();
        }
    }

    public interface CloseableIterator<T> extends Iterator<T> {
        /**
         * Must be invoked when the iterator is no longer used.
         */
        void close();
    }

    /**
     * Key comparator for DB keys.
     */
    public static class KeyComparator implements Comparator<byte[]>, Serializable {

        private static final long serialVersionUID = -7403144993786576375L;

        /**
         * Compares two DB keys.
         *
         * @param key1 first key
         * @param key2 second key
         *
         * @return comparison result
         */
        @Override
        public int compare(final byte[] key1, final byte[] key2) {
            return new BigInteger(key1).compareTo(new BigInteger(key2));
        }

    }
}

