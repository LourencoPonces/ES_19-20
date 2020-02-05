package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.QuestionAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.QuizAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import java.io.*;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.ANSWERS_IMPORT_ERROR;

public class AnswersXmlImport {
	private AnswerService answerService;
	private QuestionRepository questionRepository;
	private QuizRepository quizRepository;
	private QuizAnswerRepository quizAnswerRepository;
	private UserRepository userRepository;

	private Map<Integer, Map<Integer,Integer>> questionMap;

	public void importAnswers(InputStream inputStream, AnswerService answerService, QuestionRepository questionRepository,
							  QuizRepository quizRepository,  QuizAnswerRepository quizAnswerRepository, UserRepository userRepository) {
		this.answerService = answerService;
		this.questionRepository = questionRepository;
		this.quizRepository = quizRepository;
		this.quizAnswerRepository = quizAnswerRepository;
		this.userRepository = userRepository;

		SAXBuilder builder = new SAXBuilder();
		builder.setIgnoringElementContentWhitespace(true);

		Document doc;
		try {
			Reader reader = new InputStreamReader(inputStream, Charset.defaultCharset());
			doc = builder.build(reader);
		} catch (FileNotFoundException e) {
			throw new TutorException(ANSWERS_IMPORT_ERROR, "File not found");
		} catch (JDOMException e) {
			throw new TutorException(ANSWERS_IMPORT_ERROR, "Coding problem");
		} catch (IOException e) {
			throw new TutorException(ANSWERS_IMPORT_ERROR, "File type or format");
		}

		if (doc == null) {
			throw new TutorException(ANSWERS_IMPORT_ERROR, "File not found ot format error");
		}

		loadQuestionMap();

		importQuizAnswers(doc);
	}

	private void loadQuestionMap() {
		questionMap = questionRepository.findAll().stream()
				.collect(Collectors.toMap(Question::getKey,
						question -> question.getOptions().stream()
								.collect(Collectors.toMap(Option::getSequence,Option::getId))));
	}

	public void importAnswers(String answersXml, AnswerService answerService, QuestionRepository questionRepository,
							  QuizRepository quizRepository, QuizAnswerRepository quizAnswerRepository, UserRepository userRepository) {
		SAXBuilder builder = new SAXBuilder();
		builder.setIgnoringElementContentWhitespace(true);

		InputStream stream = new ByteArrayInputStream(answersXml.getBytes());

		importAnswers(stream, answerService, questionRepository, quizRepository, quizAnswerRepository, userRepository);
	}

	private void importQuizAnswers(Document doc) {
		XPathFactory xpfac = XPathFactory.instance();
		XPathExpression<Element> xp = xpfac.compile("//quizAnswers/quizAnswer", Filters.element());
		for (Element element : xp.evaluate(doc)) {
			importQuizAnswer(element);
		}
	}

	private void importQuizAnswer(Element answerElement) {
		LocalDateTime answerDate = null;
		if (answerElement.getAttributeValue("answerDate") != null) {
			answerDate = LocalDateTime.parse(answerElement.getAttributeValue("answerDate"));
		}

		boolean completed = false;
		if (answerElement.getAttributeValue("completed") != null) {
			completed = Boolean.parseBoolean(answerElement.getAttributeValue("completed"));
		}

		Integer quizKey = Integer.valueOf(answerElement.getChild("quiz").getAttributeValue("key"));
		Quiz quiz = quizRepository.findByKey(quizKey)
				.orElseThrow(() -> new TutorException(ANSWERS_IMPORT_ERROR,
						"quiz id does not exist " + quizKey));

		Integer key = Integer.valueOf(answerElement.getChild("user").getAttributeValue("key"));
		User user = userRepository.findByKey(key);

		QuizAnswerDto quizAnswerDto = answerService.createQuizAnswer(user.getId(), quiz.getId());
		QuizAnswer quizAnswer = quizAnswerRepository.findById(quizAnswerDto.getId()).get();
		quizAnswer.setAnswerDate(answerDate);
		quizAnswer.setCompleted(completed);

		Map<Integer,Integer> mapQuizQuestionId = quiz.getQuizQuestions().stream()
				.collect(Collectors.toMap(QuizQuestion::getSequence, QuizQuestion::getId));

		importQuestionAnswers(answerElement.getChild("questionAnswers"), user, mapQuizQuestionId, quizAnswer);
	}

	private void importQuestionAnswers(Element questionAnswersElement, User user, Map<Integer,Integer> mapQuizQuestionId, QuizAnswer quizAnswer) {
		QuizAnswerDto quizAnswerDto = new QuizAnswerDto();
        quizAnswerDto.setId(quizAnswer.getId());
        quizAnswerDto.setAnswerDate(quizAnswer.getAnswerDate());

		for (Element questionAnswerElement: questionAnswersElement.getChildren("questionAnswer")) {
			Integer timeTaken = null;
			if (questionAnswerElement.getAttributeValue("timeTaken") != null) {
				timeTaken = Integer.valueOf(questionAnswerElement.getAttributeValue("timeTaken"));
			}

			Integer answerSequence = Integer.valueOf(questionAnswerElement.getAttributeValue("sequence"));
			Integer questionSequence = Integer.valueOf(questionAnswerElement.getChild("quizQuestion").getAttributeValue("sequence"));
			Integer quizQuestionId = mapQuizQuestionId.get(questionSequence);

			Integer optionId = null;
			if (questionAnswerElement.getChild("option") != null) {
				Integer questionKey = Integer.valueOf(questionAnswerElement.getChild("option").getAttributeValue("questionKey"));
				Integer optionSequence = Integer.valueOf(questionAnswerElement.getChild("option").getAttributeValue("sequence"));
				optionId = questionMap.get(questionKey).get(optionSequence);
			}

			QuestionAnswerDto questionAnswerDto = new QuestionAnswerDto();
            questionAnswerDto.setTimeTaken(timeTaken);
            questionAnswerDto.setSequence(answerSequence);
            questionAnswerDto.setQuizQuestionId(quizQuestionId);
            questionAnswerDto.setOptionId(optionId);
            quizAnswerDto.addQuestionAnswer(questionAnswerDto);
		}
		answerService.concludeQuiz(user, quizAnswer.getQuiz().getId());
		//TODO this method is incorrect
	}

}
