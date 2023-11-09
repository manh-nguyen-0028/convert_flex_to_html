package mxml.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class XhtmlConfig {
    private String filePath;
    private String fileName;
    private String cssWith;
    private String cssHeight;
    private boolean isGenerateForm;

    public XhtmlConfig(String filePath, String fileName) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.isGenerateForm = true;
    }
}
