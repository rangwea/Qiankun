package com.wikia.calabash.ftp;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Pattern;

/**
 * @author wikia
 * @since 5/17/2021 2:43 PM
 */
@Slf4j
public class EasyFTPClient {
    private String host;
    private int port;
    private String username;
    private String password;
    private FTPClient ftpClient;

    public EasyFTPClient(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public EasyFTPClient connect() {
        log.info("get ftp client:host={};port={};username={};password={}", host, port, username, password);
        try {
            if (this.ftpClient != null && ftpClient.isConnected()) {
                return this;
            }
            FTPClient ftp = new FTPClient();
            ftp.connect(host, port);
            ftp.login(username, password);
            ftp.setConnectTimeout(50000);
            ftp.setControlEncoding("UTF-8");
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                log.warn("connect ftp fail");
                ftp.disconnect();
                throw new FTPException(String.format("ftp connect reply error:code=%s", ftp.getReplyCode()));
            }

            log.info("ftp connect success");

            this.ftpClient = ftp;
            return this;
        } catch (Exception e) {
            throw new FTPException("ftp connect exception", e);
        }
    }

    public FTPClient getClient() {
        return this.ftpClient;
    }

    public boolean checkFileExists(String parent, Pattern filePattern) {
        try {
            FTPFile[] ftpFiles = ftpClient.listFiles(parent);
            for (FTPFile ftpFile : ftpFiles) {
                String name = ftpFile.getName();
                if (filePattern.matcher(name).find()) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            throw new FTPException("ftp connect exception", e);
        }
    }

    public boolean checkFileExists(String parent, String filePattern, MatchType matchType) {
        try {
            FTPFile[] ftpFiles = ftpClient.listFiles(parent);
            for (FTPFile ftpFile : ftpFiles) {
                String name = ftpFile.getName();
                switch (matchType) {
                    case STARTS_WITH:
                        return name.startsWith(filePattern);
                    case ENDS_WITH:
                        return name.endsWith(filePattern);
                    case FULL_MATCH:
                        return name.equals(filePattern);
                    default:
                        throw new IllegalArgumentException("not support match type");
                }
            }
            return false;
        } catch (Exception e) {
            throw new FTPException("ftp connect exception", e);
        }
    }

    public byte[] readSingleFile(String remoteFilePath) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        ftpClient.retrieveFile(remoteFilePath, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public boolean downloadSingleFile(String remoteFilePath, String savePath) throws IOException {
        File downloadFile = new File(savePath);
        downloadFile.getParentFile().mkdirs();

        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile))) {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            return ftpClient.retrieveFile(remoteFilePath, outputStream);
        }
    }

    public void downloadDirectory(String baseDir, String currentRelativeDir, String localBaseDir) throws IOException {
        String dirToList = baseDir + currentRelativeDir;

        FTPFile[] subFiles = ftpClient.listFiles(dirToList);

        if (subFiles != null && subFiles.length > 0) {
            for (FTPFile curFile : subFiles) {
                String currentFileName = curFile.getName();
                if (currentFileName.equals(".") || currentFileName.equals("..")) {
                    // skip parent directory and the directory itself
                    continue;
                }

                if (curFile.isDirectory()) {
                    // download the sub directory
                    String nextRelativeDir = currentRelativeDir + currentFileName + "/";
                    downloadDirectory(baseDir, nextRelativeDir, localBaseDir);
                } else {
                    // download the file
                    String ftpFilePath = baseDir + currentRelativeDir + currentFileName;
                    String localFilePath = localBaseDir + currentRelativeDir + currentFileName;
                    downloadSingleFile(ftpFilePath, localFilePath);
                }
            }
        }
    }
}
