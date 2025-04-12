package application;

import static org.junit.Assert.*;
import org.junit.Test;
import java.time.LocalDateTime;

public class StaffCommentTest {

    @Test
    public void staffNameGetAndSetTest() {
        StaffComment comment = new StaffComment("InitialName", "Comment", "genID", LocalDateTime.now(), true);
        assertEquals("InitialName", comment.getStaffName());
        comment.setStaffName("UpdatedName");
        assertEquals("UpdatedName", comment.getStaffName());
        System.out.println("Staff name updated: " + comment.getStaffName());
    }

    @Test
    public void commentTextGetAndSetTest() {
        StaffComment comment = new StaffComment("Name", "Original comment", "genID", LocalDateTime.now(), false);
        assertEquals("Original comment", comment.getComment());
        comment.setComment("Edited comment");
        assertEquals("Edited comment", comment.getComment());
        System.out.println("Comment text updated: " + comment.getComment());
    }

    @Test
    public void genericIdGetAndSetTest() {
        StaffComment comment = new StaffComment("Name", "Comment", "originalID", LocalDateTime.now(), true);
        assertEquals("originalID", comment.getGenericId());
        comment.setGenericId("newID");
        assertEquals("newID", comment.getGenericId());
        System.out.println("Generic ID updated: " + comment.getGenericId());
    }

    @Test
    public void isQuestionFlagTest() {
        StaffComment comment = new StaffComment("Name", "Comment", "genID", LocalDateTime.now(), true);
        assertEquals(true, comment.isQuestion());
        System.out.println("Is question flag correctly set to: " + comment.isQuestion());
    }
}