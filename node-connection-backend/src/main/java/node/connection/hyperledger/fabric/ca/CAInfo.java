package node.connection.hyperledger.fabric.ca;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CAInfo {
    private String name;
    private String url;
    private boolean allowAllHostNames;
    private String pemFile;

    private CAInfo(Builder builder) {
        setName(builder.name);
        setUrl(builder.url);
        setAllowAllHostNames(builder.allowAllHostNames);
        setPemFile(builder.pemFile);
    }

    public static final class Builder {
        private String name;
        private String url;
        private boolean allowAllHostNames = false;
        private String pemFile = null;

        public Builder() {
        }

        public Builder name(String val) {
            name = val;
            return this;
        }

        public Builder url(String val) {
            url = val;
            return this;
        }

        public Builder allowAllHostNames(boolean val) {
            allowAllHostNames = val;
            return this;
        }

        public Builder pemFile(String val) {
            pemFile = val;
            return this;
        }

        public CAInfo build() {
            return new CAInfo(this);
        }
    }
}
