package redmonkey;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;


public class PatternedUrlTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();


    @Test
    public void matchStringAtTheEnd(){

        //  given
        PatternedUrl pattern = new PatternedUrl("/bestsellers/{S}");

        //  when
        boolean matched1 = pattern.matches("/bestsellers/a");
        boolean matched2 = pattern.matches("/bestsellers/Z");
        boolean matched3 = pattern.matches("/bestsellers/Abcdef");

        //  then
        assertThat(matched1).isTrue();
        assertThat(matched2).isTrue();
        assertThat(matched3).isTrue();
    }

    @Test
    public void matchStringInTheMiddle(){

        //  given
        PatternedUrl pattern = new PatternedUrl("/users/{S}/sessions");

        //  when
        boolean matched1 = pattern.matches("/users/John/sessions");
        boolean matched2 = pattern.matches("/users/U/sessions");
        boolean matched3 = pattern.matches("/users/null/sessions");

        //  then
        assertThat(matched1).isTrue();
        assertThat(matched2).isTrue();
        assertThat(matched3).isTrue();
    }

    @Test
    public void matchStringOnStart(){

        //  given
        PatternedUrl pattern = new PatternedUrl("{S}01234");

        //  when
        boolean matched1 = pattern.matches("super01234");
        boolean matched2 = pattern.matches("A01234");
        boolean matched3 = pattern.matches("Z01234");

        //  then
        assertThat(matched1).isTrue();
        assertThat(matched2).isTrue();
        assertThat(matched3).isTrue();
    }

    @Test
    public void matchWithOneNumberAfterString(){

        //  given
        PatternedUrl pattern = new PatternedUrl("{S}0");

        //  when
        boolean matched1 = pattern.matches("super0");
        boolean matched2 = pattern.matches("A0");
        boolean matched3 = pattern.matches("Z0");

        //  then
        assertThat(matched1).isTrue();
        assertThat(matched2).isTrue();
        assertThat(matched3).isTrue();
    }


    @Test
    public void matchOnlyString(){

        //  given
        PatternedUrl pattern = new PatternedUrl("{S}");

        //  when
        boolean matched1 = pattern.matches("super");
        boolean matched2 = pattern.matches("A");
        boolean matched3 = pattern.matches("Z");

        //  then
        assertThat(matched1).isTrue();
        assertThat(matched2).isTrue();
        assertThat(matched3).isTrue();
    }

    @Test
    public void notMatchEmptyUrlToNumberPattern(){

        //  given
        PatternedUrl pattern = new PatternedUrl("{D}");

        //  when
        boolean matched1 = pattern.matches("");

        //  then
        assertThat(matched1).isFalse();
    }


    @Test
    public void matchEmptyUrlToEmptyPattern(){

        //  given
        PatternedUrl pattern = new PatternedUrl("");

        //  when
        boolean matched1 = pattern.matches("");


        //  then
        assertThat(matched1).isTrue();
    }

    @Test
    public void notMatchIfNumberExpectedAtEndButNotFound(){
        //  given
        PatternedUrl pattern = new PatternedUrl("/bestsellers/{D}");

        //  when
        boolean notMatched4 = pattern.matches("/bestsellers/");

        //  then
        assertThat(notMatched4).isFalse();
    }

    @Test
    public void throwIAEOnUnsupportedSpecialCharacter(){

        //  then
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Unsupported special character (X) at position: 14");

        //  when
        new PatternedUrl("/bestsellers/{X}");
    }

    @Test
    public void throwIAEOnUnfinishedSpecialCharacter(){

        //  then
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Unfinished closure");

        //  when
        new PatternedUrl("/bestsellers/{D");
    }


    @Test
    public void throwIAEOnMalformedStart(){

        //  then
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Invalid syntax ({) at position: 14");

        //  when
        new PatternedUrl("/bestsellers/{{D}");
    }

    @Test
    public void throwIllegalArgumentExceptionOnMalformedStart2(){

        //  given
        PatternedUrl pattern = new PatternedUrl("/bestsellers/}{D}");

        //  when
        boolean notMatched4 = pattern.matches("/bestsellers/");

        //  then
        assertThat(notMatched4).isFalse();
    }

    @Test
    public void throwIllegalArgumentExceptionOnMalformedEnd2(){
//  given
        PatternedUrl pattern = new PatternedUrl("/bestsellers/{D}}");

        //  when
        boolean notMatched4 = pattern.matches("/bestsellers/");

        //  then
        assertThat(notMatched4).isFalse();
    }

    @Test
    public void throwIllegalArgumentExceptionOnMalformedEnd(){
//  given
        PatternedUrl pattern = new PatternedUrl("/bestsellers/{D}{");

        //  when
        boolean notMatched4 = pattern.matches("/bestsellers/");

        //  then
        assertThat(notMatched4).isFalse();
    }


    @Test
    public void matchOnlyNumber(){

        //  given
        PatternedUrl pattern = new PatternedUrl("{D}");

        //  when
        boolean matched1 = pattern.matches("0");
        boolean matched2 = pattern.matches("0123456789");

        //  then
        assertThat(matched1).isTrue();
        assertThat(matched2).isTrue();
    }

    @Test
    public void matchStringAfterNumberWithSeparator(){
        //  given
        PatternedUrl pattern = new PatternedUrl("{D}-{S}");

        //  when
        boolean matched1 = pattern.matches("0-a");
        boolean matched2 = pattern.matches("12-abZ");

        //  then
        assertThat(matched1).isTrue();
        assertThat(matched2).isTrue();
    }

    @Test
    public void matchNumberAfterStringWithSeparator(){
        //  given
        PatternedUrl pattern = new PatternedUrl("{S}-{D}");

        //  when
        boolean matched1 = pattern.matches("a-0");
        boolean matched2 = pattern.matches("ABcD-123");

        //  then
        assertThat(matched1).isTrue();
        assertThat(matched2).isTrue();
    }

    @Test
    public void matchWithNumberOnStart(){
        //  given
        PatternedUrl pattern = new PatternedUrl("{D}abcdef");

        //  when
        boolean matched1 = pattern.matches("0abcdef");
        boolean matched2 = pattern.matches("123abcdef");

        //  then
        assertThat(matched1).isTrue();
        assertThat(matched2).isTrue();
    }

    @Test
    public void matchNumberAfterNumberWithSeparator(){

        //  given
        PatternedUrl pattern = new PatternedUrl("{D}-{D}");

        //  when
        boolean matched1 = pattern.matches("0-0");
        boolean matched2 = pattern.matches("12-123");

        //  then
        assertThat(matched1).isTrue();
        assertThat(matched2).isTrue();
    }

    @Test
    public void notMatchNumberAfterNumberWithoutSeparator(){

        //  given
        PatternedUrl pattern = new PatternedUrl("{D}{D}");

        //  when
        boolean matched1 = pattern.matches("00");
        boolean matched2 = pattern.matches("1");

        //  then
        assertThat(matched1).isFalse();
        assertThat(matched2).isFalse();
    }

    @Test
    public void notMatchEmptyStringIfNumberExpected(){

        //  given
        PatternedUrl pattern = new PatternedUrl("{D}");

        //  when
        boolean nonMatched = pattern.matches("");

        //  then
        assertThat(nonMatched).isFalse();
    }

    @Test
    public void notMatchIfNumberExpectedAtEndButMoreFound(){

        //  given
        PatternedUrl pattern = new PatternedUrl("/bestsellers/{D}");

        //  when
        boolean notMatched1 = pattern.matches("/bestsellers/1a");

        //  then
        assertThat(notMatched1).isFalse();
    }

    @Test
    public void notMatchIfNumberExpectedAtEndButNonNumberFound(){

        //  given
        PatternedUrl pattern = new PatternedUrl("/bestsellers/{D}");

        //  when
        boolean notMatched1 = pattern.matches("/bestsellers/a");
        boolean notMatched2 = pattern.matches("/bestsellers/A");
        boolean notMatched3 = pattern.matches("/bestsellers/O");
        boolean notMatched5 = pattern.matches("/bestsellers//");

        //  then
        assertThat(notMatched1).isFalse();
        assertThat(notMatched2).isFalse();
        assertThat(notMatched3).isFalse();
        assertThat(notMatched5).isFalse();
    }

    @Test
    public void notMatchNumberAtEnd(){

        //  given
        PatternedUrl pattern = new PatternedUrl("/bestsellers/{D}");

        //  when
        boolean notMatched6 = pattern.matches("/1");
        boolean notMatched7 = pattern.matches("");
        boolean notMatched8 = pattern.matches(" ");

        //  then
        assertThat(notMatched6).isFalse();
        assertThat(notMatched7).isFalse();
        assertThat(notMatched8).isFalse();
    }


}