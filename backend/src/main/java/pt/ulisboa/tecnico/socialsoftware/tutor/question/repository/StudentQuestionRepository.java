package pt.ulisboa.tecnico.socialsoftware.tutor.question.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.transaction.Transactional;

import java.util.List;

@Repository
@Transactional
public interface StudentQuestionRepository extends JpaRepository<StudentQuestion, Integer> {

    @Query(value = "SELECT MAX(student_question_key) FROM student_question", nativeQuery = true)
    Integer getMaxQuestionNumber();

    @Query(value = "SELECT * FROM student_question sq WHERE sq.user_id = :userId ", nativeQuery = true)
    List<StudentQuestion> findByUser(Integer userId);

    @Query(value = "SELECT * FROM student_question sq, questions q WHERE q.course_id = :courseId AND sq.id=q.id", nativeQuery = true)
    List<StudentQuestion> findByCourse(Integer courseId);

    @Query(value = "SELECT * FROM student_question sq NATURAL JOIN questions q WHERE q.course_id = :courseId AND sq.user_id = :userId", nativeQuery = true)
    List<StudentQuestion> findByCourseAndUser(Integer courseId, Integer userId);

    @Query(value = "SELECT * FROM student_question sq NATURAL JOIN questions q WHERE q.course_id = :courseId AND sq.user_id = :userId and sq.submitted_status = :status", nativeQuery = true)
    List<StudentQuestion> findByCourseUserAndStatus(Integer courseId, Integer userId, StudentQuestion.SubmittedStatus status);

}
