package pt.ulisboa.tecnico.socialsoftware.tutor.exceptions;

public enum ErrorMessage {

    INVALID_ACADEMIC_TERM_FOR_COURSE_EXECUTION("Invalid academic term for course execution"),
    INVALID_ACRONYM_FOR_COURSE_EXECUTION("Invalid acronym for course execution"),
    INVALID_CONTENT_FOR_OPTION("Invalid content for option"),
    INVALID_CONTENT_FOR_QUESTION("Invalid content for question"),
    INVALID_NAME_FOR_COURSE("Invalid name for course"),
    INVALID_NAME_FOR_TOPIC("Invalid name for topic"),
    INVALID_SEQUENCE_FOR_OPTION("Invalid sequence for option"),
    INVALID_SEQUENCE_FOR_QUESTION_ANSWER("Invalid sequence for question answer"),
    INVALID_TITLE_FOR_ASSESSMENT("Invalid title for assessment"),
    INVALID_TITLE_FOR_QUESTION("Invalid title for question"),
    INVALID_URL_FOR_IMAGE("Invalid url for image"),
    INVALID_TYPE_FOR_COURSE("Invalid type for course"),
    INVALID_TYPE_FOR_COURSE_EXECUTION("Invalid type for course execution"),
    INVALID_AVAILABLE_DATE_FOR_QUIZ("Invalid available date for quiz"),
    INVALID_CONCLUSION_DATE_FOR_QUIZ("Invalid conclusion date for quiz"),
    INVALID_RESULTS_DATE_FOR_QUIZ("Invalid results date for quiz"),
    INVALID_TITLE_FOR_QUIZ("Invalid title for quiz"),
    INVALID_TYPE_FOR_QUIZ("Invalid type for quiz"),
    INVALID_QUESTION_SEQUENCE_FOR_QUIZ("Invalid question sequence for quiz"),

    ASSESSMENT_NOT_FOUND("Assessment not found with id %d"),
    COURSE_EXECUTION_NOT_FOUND("Course execution not found with id %d"),
    OPTION_NOT_FOUND("Option not found with id %d"),
    QUESTION_ANSWER_NOT_FOUND("Question answer not found with id %d"),
    QUESTION_NOT_FOUND("Question not found with id %d"),
    STUDENT_QUESTION_NOT_FOUND("Student Question not found with id %d"),
    QUESTION_ALREADY_READ("Question %d was already read by a teacher"),
    USER_NOT_FOUND("User not found with id %d"),
    TOPIC_NOT_FOUND("Topic not found with id %d"),
    NO_TOPICS("The question has no Topics"),
    TOPIC_CONJUNCTION_NOT_FOUND("Topic Conjunction not found with id %d"),
    TOURNAMENT_NOT_FOUND("Tournament not found with id %d"),

    INVALID_JUSTIFICATION("The justification \"%s\" is invalid"),
    CANNOT_REJECT_ACCEPTED_SUGGESTION("Cannot reject already accepted suggestion"),
    CANNOT_REJECT_WITHOUT_JUSTIFICATION("You must justify why the question is rejected"),
    CANNOT_EVALUATE_PROMOTED_QUESTION("Cannot evaluate promoted question"),
    INVALID_STATUS("Non existing status %s"),
    QUIZ_ANSWER_NOT_FOUND("Quiz answer not found with id %d"),
    QUIZ_NOT_FOUND("Quiz not found with id %d"),
    QUIZ_QUESTION_NOT_FOUND("Quiz question not found with id %d"),
    COURSE_NOT_FOUND("Course not found with name %s"),

    CANNOT_DELETE_COURSE_EXECUTION("The course execution cannot be deleted %s"),
    USERNAME_NOT_FOUND("Username %d not found"),

    QUIZ_USER_MISMATCH("Quiz %s is not assigned to student %s"),
    QUIZ_MISMATCH("Quiz Answer Quiz %d does not match Quiz Question Quiz %d"),
    QUESTION_OPTION_MISMATCH("Question %d does not have option %d"),
    COURSE_EXECUTION_MISMATCH("Course Execution %d does not have quiz %d"),
    NO_OPTIONS("The question has no options"),
    NO_CORRECT_OPTIONS("The question doesn't have any correct options"),

    DUPLICATE_TOPIC("Duplicate topic: %s"),
    DUPLICATE_USER("Duplicate user: %s"),
    DUPLICATE_COURSE_EXECUTION("Duplicate course execution: %s"),

    USERS_IMPORT_ERROR("Error importing users: %s"),
    QUESTIONS_IMPORT_ERROR("Error importing questions: %s"),
    TOPICS_IMPORT_ERROR("Error importing topics: %s"),
    ANSWERS_IMPORT_ERROR("Error importing answers: %s"),
    QUIZZES_IMPORT_ERROR("Error importing quizzes: %s"),

    QUESTION_IS_USED_IN_QUIZ("Question is used in quiz %s"),
    USER_NOT_ENROLLED("%s - Not enrolled in any available course"),
    QUIZ_NO_LONGER_AVAILABLE("This quiz is no longer available"),
    QUIZ_NOT_YET_AVAILABLE("This quiz is not yet available"),

    NO_CORRECT_OPTION("Question does not have a correct option"),
    NOT_ENOUGH_QUESTIONS("Not enough questions to create a quiz"),
    QUESTION_MISSING_DATA("Missing information for question"),
    QUESTION_MULTIPLE_CORRECT_OPTIONS("Questions can only have 1 correct option"),
    QUESTION_CHANGE_CORRECT_OPTION_HAS_ANSWERS("Can not change correct option of answered question"),
    ONE_CORRECT_OPTION_NEEDED("Questions need to have 1 and only 1 correct option"),
    CANNOT_CHANGE_ANSWERED_QUESTION("Can not change answered question"),
    QUIZ_HAS_ANSWERS("Quiz already has answers"),
    QUIZ_ALREADY_COMPLETED("Quiz already completed"),
    QUIZ_ALREADY_STARTED("Quiz was already started"),
    QUIZ_QUESTION_HAS_ANSWERS("Quiz question has answers"),
    FENIX_ERROR("Fenix Error"),
    AUTHENTICATION_ERROR("Authentication Error"),
    FENIX_CONFIGURATION_ERROR("Incorrect server configuration files for fenix"),

    TOURNAMENT_NOT_CONSISTENT("Field %s of tournament is not consistent"),
    TOURNAMENT_NOT_AVAILABLE("There aren't any available tournaments"),
    NO_TOURNAMENT_QUIZ("There isn't any quiz generated for this tournament"),
    TOURNAMENT_CANNOT_BE_CANCELED("Tournament with %s status cannot be canceled"),
    USER_ALREADY_SIGNED_UP_IN_TOURNAMENT("User already signed-up in tournament"),
    TOURNAMENT_CREATED_BY_NON_STUDENT("Tournament created by non-student"),
    USER_NOT_ENROLLED_IN_COURSE_EXECUTION("User not enrolled in course execution %s"),
    MISSING_TOURNAMENT_OWNERSHIP("User did not create this tournament"),



    ACCESS_DENIED("You do not have permission to view this resource"),
    CANNOT_OPEN_FILE("Cannot open file"),

    CLARIFICATION_REQUEST_MISSING_CONTENT("Missing content of clarification request"),
    QUESTION_NOT_ANSWERED_BY_STUDENT("Question not answered yet"),
    DUPLICATE_CLARIFICATION_REQUEST("%s already submitted a clarification request for this question"),
    CLARIFICATION_REQUEST_NOT_FOUND("Clarification Request not found"),
    CLARIFICATION_REQUEST_NOT_SUBMITTED("Student %s didn't submit a clarification request"),
    CLARIFICATION_REQUEST_NOT_EMPTY("Clarification Request has more than the initial message"),
    CLARIFICATION_MESSAGE_MISSING_CONTENT("Missing content of clarification request answer"),
    CLARIFICATION_MESSAGE_NOT_FOUND("Clarification Message not found"),

    INVALID_STUDENT_QUESTION_EVALUATION("Invalid StudentQuestion Evaluation"),

    NO_MY_STATS_FOUND("No stats with id %d");

    public final String label;

    ErrorMessage(String label) {
        this.label = label;
    }
}