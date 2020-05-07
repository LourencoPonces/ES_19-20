delete from topics_questions where questions_id in (select id from student_question);
delete from options where question_id in (select id from student_question);
with a as (
    delete
    from student_question
    returning id
)
delete
    from questions
    where id in (
        select id from a
    )
;
