package badmonkey.cache;

import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.Test;

import static badmonkey.BadmonkeyLibraryAssertions.assertThat;

/**
 * Created by urwisy on 2015-04-11.
 */
public class EntryTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void throwNPEonNulledContent(){

        //  then
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Content to put into cache should not be null");

        //  when
        new Entry(null, "sometag");
    }

    @Test
    public void throwNPEonNulledEtag(){

        //  then
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Etag to put into cache should not be null");

        //  when
        new Entry("sometag", null);
    }

    @Test
    public void initializeEntryWithContentAndEtag(){

        //  given
        final String content = "[]";
        final String etag = "980";

        //  when
        Entry entry = new Entry(content, etag);

        //  then
        assertThat(entry).hasContent(content).hasEtag(etag);
    }

}