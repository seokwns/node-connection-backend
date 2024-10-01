package node.connection.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RegistryDocument {
    @JsonProperty("id")
    private String id;

    @JsonProperty("address")
    private String address;

    @JsonProperty("detailAddress")
    private String detailAddress;

    @JsonProperty("titleSection")
    private TitleSection titleSection;

    @JsonProperty("exclusivePartDescription")
    private ExclusivePartDescription exclusivePartDescription;

    @JsonProperty("firstSection")
    private List<FirstSection> firstSection;

    @JsonProperty("secondSection")
    private List<SecondSection> secondSection;
}
