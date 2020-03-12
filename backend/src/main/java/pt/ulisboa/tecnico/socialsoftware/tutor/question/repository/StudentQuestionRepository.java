package pt.ulisboa.tecnico.socialsoftware.tutor.question.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;

import javax.transaction.Transactional;

import java.util.Optional;

@Repository
@Transactional
public interface StudentQuestionRepository extends JpaRepository<StudentQuestion, Integer> {

    @Query(value = "SELECT MAX(student_question_key) FROM student_question", nativeQuery = true)
    Integer getMaxQuestionNumber();

    @Query(value = "SELECT * FROM student_question sq WHERE sq.student_question_key = :key", nativeQuery = true)
    Optional<StudentQuestion> findByKey(Integer key);
}
