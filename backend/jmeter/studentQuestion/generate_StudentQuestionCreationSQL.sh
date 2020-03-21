#!/bin/sh

START=10001
END=11000
TOPIC_CONTENT="EVAL_STUDENT_QUESTION_OPTION"
COURSE_ID=3

echo ""
#Criar pergunta
for qid in $(seq $START $END); do
    #echo "INSERT INTO questions(id, title, content) VALUES(${qid}, 'Question${qid}', 'Content${qid}');"
    echo "INSERT INTO questions(id, title, content, course_id, status) VALUES(${qid}, 'Question${qid}', 'Content${qid}', 2, 'DISABLED');"
    #echo "INSERT INTO questions(id, title, content, course_id) VALUES(${qid}, 'Question${qid}', 'Content${qid}', \${courseId});"
    echo "INSERT INTO topics_questions(topics_id, questions_id) VALUES(125, ${qid});"
    echo "INSERT INTO options(question_id, content, correct) VALUES(${qid}, '${TOPIC_CONTENT}', true);"
    echo "INSERT INTO student_question(id, user_id, submitted_status, student_question_key) VALUES(${qid}, 676, 'WAITING_FOR_APPROVAL', ${qid});"
    echo ""
done

echo ""
echo ""
echo ""
echo ""

# Cleanup
# apagar primeiro a student question e depois a question
echo "DELETE FROM student_question;"
echo "DELETE FROM options where content = '${TOPIC_CONTENT}';"
echo "DELETE FROM topics_questions where questions_id >= ${START} and questions_id <= ${END};"
echo "DELETE FROM questions where id >= ${START} and id <= ${END};"
