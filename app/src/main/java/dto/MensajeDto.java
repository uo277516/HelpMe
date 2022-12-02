package dto;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;

import java.time.LocalDateTime;

public class MensajeDto {
    public String contenido;

    @Exclude
    public LocalDateTime fechaEnvio;

    @PropertyName("created_at")
    public String createdAt;

    @Exclude
    public String userUid;

    @PropertyName("type")
    public String mimeType;
}
