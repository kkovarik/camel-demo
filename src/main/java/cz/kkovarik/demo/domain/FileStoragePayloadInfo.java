package cz.kkovarik.demo.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


/**
 * @author <a href="mailto:kovarikkarel@gmail.com">Karel Kovarik</a>
 */
public class FileStoragePayloadInfo {

    private final String fileName;
    private final String content;

    public FileStoragePayloadInfo(String fileName, String content) {
        this.fileName = fileName;
        this.content = content;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("fileName", fileName)
                .append("content", content)
                .toString();
    }
}
