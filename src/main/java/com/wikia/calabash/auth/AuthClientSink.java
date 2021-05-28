package com.wikia.calabash.auth;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

/**
 * @author wikia
 * @since 4/20/2021 2:57 PM
 */
@Getter
@Setter
@ToString
public class AuthClientSink {
    private Long authClientId;
    private Long sinkId;

    public AuthClientSink(Long authClientId, Long sinkId) {
        this.authClientId = authClientId;
        this.sinkId = sinkId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthClientSink that = (AuthClientSink) o;
        return Objects.equals(authClientId, that.authClientId) &&
                Objects.equals(sinkId, that.sinkId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authClientId, sinkId);
    }
}
