package edu.oregonstate.mist.api.textbooks

import com.google.common.base.Optional

import edu.oregonstate.mist.textbooksapi.TextbooksCollector
import edu.oregonstate.mist.textbooksapi.core.Textbook
import edu.oregonstate.mist.textbooksapi.resources.TextbooksResource

import org.junit.Test

class TextbooksResourceTest {

    Textbook textbook = new Textbook(
            id: "1111111111111",
            coverImageUrl: "//coverimages.verbacompete.com/no_image.jpg",
            title: "Example title",
            author: "Example author",
            edition: 1,
            copyrightYear: 2018,
            priceNewUSD: 9999.99,
            priceUsedUSD: 1.00
    )
    List<Textbook> textbookList = [textbook, textbook, textbook]
    TextbooksResource resource = new TextbooksResource()

    // Test getTextbooks

    // Test using all arguments
    @Test
    void testAllArgs() {
        TextbooksCollector.metaClass.static.getTextbooks = getMockCollectorClosure(true)
        def res = resource.getTextbooks("Term", "AAA", "111", Optional.of("111"))
        validateResponse(res, 200, null, true)
    }

    // Test no textbooks found
    @Test
    void testNoBooksFound() {
        TextbooksCollector.metaClass.static.getTextbooks = getMockCollectorClosure(false)
        def res = resource.getTextbooks("Term", "AAA", "111", Optional.of("111"))
        validateResponse(res, 200,  null, false)
        assert res.entity.data == []
    }

    // Test not using section
    @Test
    void testNoSection() {
        TextbooksCollector.metaClass.static.getTextbooksNoSection = {
            String term, String subject, String courseNumber -> textbookList
        }
        TextbooksCollector.metaClass.static.getTextbooks = getMockCollectorClosure(true)
        def res = resource.getTextbooks("Term", "AAA", "111", Optional.absent())
        validateResponse(res, 200, null, true)
    }

    // Test not using required fields
    @Test
    void testBadRequest() {
        TextbooksCollector.metaClass.static.getTextbooks = getMockCollectorClosure(true)
        def badRequests = [
                resource.getTextbooks(null, "AAA", "111", Optional.of("111")),
                resource.getTextbooks("Term", null, "111", Optional.of("111")),
                resource.getTextbooks("Term", "AAA", null, Optional.of("111"))
        ]
        badRequests.each {
            validateResponse(it, 400, "Query must contain term, subject, and courseNumber", false)
        }
    }

    /**
     * Validates a response given certain requirements
     *
     * @param res Response object
     * @param code HTTP status code
     * @param message If not null, will validate message is found in developerMessage field
     * @param validBooks If true, will validate that response correlates to textbooksList
     */
    void validateResponse(def res, int code, String message, boolean validBooks) {
        assert res.status == code
        if(message) {
            assert res.entity.developerMessage.contains(message)
        }
        if(validBooks) {
            assert res.entity.data.size() == textbookList.size()
            assert res.entity.data[0].id == textbook.id
            assert res.entity.data[0].attributes == textbook
        }
    }

    /**
     * Creates a closure for mocking the TextbooksCollector.getTextbooks method
     *
     * @param nonEmpty
     * @return
     */
    Closure getMockCollectorClosure(boolean nonEmpty) {
        if(nonEmpty) {
            { String term, String subject, String courseNumber, String section -> textbookList }
        } else {
            { String term, String subject, String courseNumber, String section -> [] }
        }
    }
}
