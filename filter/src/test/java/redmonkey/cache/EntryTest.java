package redmonkey.cache;

import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.Test;

import static redmonkey.BadmonkeyLibraryAssertions.assertThat;
import static redmonkey.cache.TestValues.UTF8_JSON;

/**
 * Created by jszczepankiewicz on 2015-04-11.
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
        new Entry(null, "sometag", UTF8_JSON);
    }

    @Test
    public void throwNPEonNulledEtag(){

        //  then
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Etag to put into cache should not be null");

        //  when
        new Entry("sometag", null, UTF8_JSON);
    }

    @Test
    public void initializeEntryWithContentAndEtag(){

        //  given
        final String content = "[]";
        final String etag = "980";

        //  when
        Entry entry = new Entry(content, etag, UTF8_JSON);

        //  then
        assertThat(entry).hasContent(content).hasEtag(etag);
    }

}