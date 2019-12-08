package si.fri.rso.lib;

public class DTOCatalog {
    private CatalogFileMetadata data;
    private String message;
    private Integer status;

    public CatalogFileMetadata getData() {
        return data;
    }

    public Integer getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public void setData(CatalogFileMetadata data) {
        this.data = data;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
