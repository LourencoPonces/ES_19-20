package pt.ulisboa.tecnico.socialsoftware.tutor.question.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface StudentQuestionRepository extends JpaRepository<StudentQuestion, Integer> {
}
