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
    Closure validTermClosure = {String academicYear, String term -> null}

    // Test getTextbooks

    // Test using all arguments
    @Test
    void testAllArgs() {
        TextbooksCollector.metaClass.getTextbooks = getMockCollectorClosure(true)
        TextbooksResource resource = getTextbooksResource()
        def res = resource.getTextbooks("2018", "Term", "AAA", "111", Optional.of("111"))
        validateResponse(res, 200, null, true)
    }

    // Test no textbooks found
    @Test
    void testNoBooksFound() {
        TextbooksCollector.metaClass.getTextbooks = getMockCollectorClosure(false)
        TextbooksResource resource = getTextbooksResource()
        def res = resource.getTextbooks("2018", "Term", "AAA", "111", Optional.of("111"))
        validateResponse(res, 200,  null, false)
        assert res.entity.data == []
    }

    // Test not using section
    @Test
    void testNoSection() {
        TextbooksCollector.metaClass.getTextbooksNoSection = {
            String term, String subject, String courseNumber -> textbookList
        }
        TextbooksCollector.metaClass.getTextbooks = getMockCollectorClosure(true)
        TextbooksResource resource = getTextbooksResource()
        def res = resource.getTextbooks("2018", "Term", "AAA", "111", Optional.absent())
        validateResponse(res, 200, null, true)
    }

    // Test invalid requests
    @Test
    void testBadRequest() {
        TextbooksCollector.metaClass.getTextbooks = getMockCollectorClosure(true)
        TextbooksResource resource = getTextbooksResource()

        // Test not using required fields
        def badRequests = [
                resource.getTextbooks(null, "Term", "AAA", "111", Optional.of("111")),
                resource.getTextbooks("2018", null, "AAA", "111", Optional.of("111")),
                resource.getTextbooks("2018", "Term", null, "111", Optional.of("111")),
                resource.getTextbooks("2018", "Term", "AAA", null, Optional.of("111"))
        ]
        badRequests.each {
            validateResponse(it, 400, "Query must contain", false)
        }

        // Test using invalid patterns in fields
        def badPatterns = [
                resource.getTextbooks("(", "Term", "AAA", "111", Optional.of("111")),
                resource.getTextbooks("2018", "(", "AAA", "111", Optional.of("111")),
                resource.getTextbooks("2018", "Term", "(", "111", Optional.of("111")),
                resource.getTextbooks("2018", "Term", "AAA", "(", Optional.of("111")),
                resource.getTextbooks("2018", "Term", "AAA", "111", Optional.of("("))
        ]
        badPatterns.each {
            validateResponse(it, 400, "must match pattern", false)
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
            { String term, String subject, String courseNumber,
              Optional<String> section -> textbookList }
        } else {
            { String term, String subject, String courseNumber,
              Optional<String> section -> [] }
        }
    }

    TextbooksResource getTextbooksResource() {
        TextbooksCollector.metaClass.validateTerm = validTermClosure
        TextbooksCollector textbooksCollector = new TextbooksCollector(null, "")
        new TextbooksResource(textbooksCollector)
    }
}
