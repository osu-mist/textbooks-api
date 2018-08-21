import json
import sys
import unittest
import utils
from copy import deepcopy


class TestStringMethods(unittest.TestCase):

    def test_get_textbooks(self):
        valid_params = deepcopy(book_params)

        # Missing required parameters
        required_params = ["academicYear", "term", "subject", "courseNumber"]
        for param in required_params:
            test_missing_param(self, param, valid_params)

        # Valid course
        book_params["section"] = None
        valid_course = utils.get_textbooks(book_params)
        validate_response(self, valid_course, 200, "textbook")

        # Valid section of course
        book_params["section"] = valid_params["section"]
        valid_section = utils.get_textbooks(book_params)
        validate_response(self, valid_section, 200, "textbook")
        section_data = valid_section.json()["data"]
        course_data = valid_course.json()["data"]
        for book in section_data:
            self.assertTrue(book in course_data)

        # Invalid section of valid course
        book_params["section"] = "-99999"
        invalid_section = utils.get_textbooks(book_params)
        validate_response(self, invalid_section, 200)

        # Invalid course
        book_params["section"] = None
        book_params["courseNumber"] = "-99999"
        invalid_course = utils.get_textbooks(book_params)
        validate_response(self, invalid_course, 200)


def validate_response(self, res, code=None, res_type=None, message=None):
    if code:
        self.assertEqual(res.status_code, code)
    if res_type:
        for resource in res.json()["data"]:
            self.assertEqual(resource["type"], res_type)
    if message:
        self.assertIn(message, res.json()["developerMessage"])


def test_missing_param(self, param, valid_params):
    book_params[param] = None
    res = utils.get_textbooks(book_params)
    validate_response(self, res, 400,
                      message="Query must contain {}".format(param))
    book_params[param] = valid_params[param]


def set_course_data(subject):
    terms = utils.get_courses(None)
    term_id = terms.json()[0]["id"]
    (valid_year, valid_term) = term_id.split("-")
    courses = utils.get_courses({"term_id": term_id, "id": subject})
    valid_course = courses.json()[0]["id"]
    valid_section = courses.json()[0]["sections"][0]["name"]
    global book_params
    book_params = {
        "academicYear": valid_year,
        "term": valid_term,
        "subject": subject,
        "courseNumber": valid_course,
        "section": valid_section
    }


if __name__ == "__main__":
    namespace, args = utils.parse_args()
    config = json.load(open(namespace.input_file))
    utils.set_local_vars(config)
    sys.argv = args
    set_course_data(config["valid_subject"])
    unittest.main()
