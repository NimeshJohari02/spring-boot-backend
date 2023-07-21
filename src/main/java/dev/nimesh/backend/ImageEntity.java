package dev.nimesh.backend;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

@Entity
public class ImageEntity {

    @Id
    private String email;

    @Lob
    private byte[] imageData;

    /**
     * get field @Id
     *
     * @return email @Id

     */
    public String getEmail() {
        return this.email;
    }
    // Constructor with parameters
    public ImageEntity(String email, byte[] imageData) {
        this.email = email;
        this.imageData = imageData;
    }

    /**
     * set field @Id
     *
     * @param email @Id

     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * get field @Lob
     *
     * @return imageData @Lob

     */
    public byte[] getImageData() {
        return this.imageData;
    }

    /**
     * set field @Lob
     *
     * @param imageData @Lob

     */
    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

}
