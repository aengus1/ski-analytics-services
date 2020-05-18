package ski.crunch.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import ski.crunch.utils.Jsonable;

import java.util.HashSet;

import static ski.crunch.dao.SerialScannerTest.TABLE_NAME;

@DynamoDBTable(tableName = TABLE_NAME)  //override this on call
public class BookItem implements Jsonable {

    private String id;
    private String title;
    private String isbn;
    private String dimensions;
    private HashSet<String> authors;
    private int price;
    private int pageCount;

    public BookItem() {

    }

    @DynamoDBHashKey(attributeName = "id")
    public String getId() {
        return id;
    }

    @DynamoDBAttribute(attributeName = "Title")
    public String getTitle() {
        return title;
    }
    @DynamoDBAttribute(attributeName = "ISBN")
    public String getIsbn() {
        return isbn;
    }

    @DynamoDBAttribute(attributeName = "Dimensions")
    public String getDimensions() {
        return dimensions;
    }

    @DynamoDBAttribute(attributeName = "Authors")
    public HashSet<String> getAuthors() {
        return authors;
    }

    @DynamoDBAttribute(attributeName = "Price")
    public int getPrice() {
        return price;
    }
    @DynamoDBAttribute(attributeName = "PageCount")
    public int getPageCount() {
        return pageCount;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    public void setAuthors(HashSet<String> authors) {
        this.authors = authors;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }
}
