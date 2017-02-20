package cz.kkovarik.demo.dto;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


/**
 * @author <a href="mailto:kovarikkarel@gmail.com">Karel Kovarik</a>
 */
public class ExceptionInfoDto {

    private String message;
    private String messageId;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("message", message)
                .append("messageId", messageId)
                .toString();
    }
}
