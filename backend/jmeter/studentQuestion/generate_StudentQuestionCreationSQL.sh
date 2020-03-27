#!/bin/sh

BASE_ID=10001
NUM_ROUNDS=5

COURSE_ID=2

TOPIC_ID=1000
TOPIC_NAME="Testing topic"

OPTION_CONTENT="CHECK_STUDENT_QUESTION_STATUS_OPTION"

STATUS=("WAITING_FOR_APPROVAL" "APPROVED" "REJECTED")
# STATUS=("WAITING_FOR_APPROVAL")

JUSTIFICATION="Inappropriate question"
JUST_IND=2 # only justify questions in this index. I know... super hard coded :(


echo ""

echo "INSERT INTO topics(id, name, course_id) VALUES(${TOPIC_ID}, '${TOPIC_NAME}', ${COURSE_ID});"

echo ""

#Criar pergunta
total_questions=0
for round in `seq 0 $((${NUM_ROUNDS} - 1))`; do
    for status_ind in `seq 0 $((${#STATUS[@]} - 1))`; do
        qid=$((${BASE_ID} + ${round}*${#STATUS[@]} + ${status_ind}))
        question_status=${STATUS[${status_ind}]}
        # echo ${qid} ${question_status}

        justification=""
        if [[ ${status_ind} -eq ${JUST_IND} ]]; then
            justification=${JUSTIFICATION}
        fi
        
        echo "INSERT INTO questions(id, title, content, course_id, status) VALUES(${qid}, 'Question${qid}', 'Content${qid}', ${COURSE_ID}, 'DISABLED');"
        echo "INSERT INTO topics_questions(topics_id, questions_id) VALUES(${TOPIC_ID}, ${qid});"
        echo "INSERT INTO options(question_id, content, correct) VALUES(${qid}, '${OPTION_CONTENT}', true);"
        echo "INSERT INTO student_question(id, user_id, submitted_status, justification, student_question_key) VALUES(${qid}, 676, '${question_status}', '${justification}', ${qid});"
        echo ""

        total_questions=$((${total_questions} + 1))
    done
done

echo ""
echo ""
echo ""
echo ""

# Cleanup
# apagar primeiro a student question e depois a question
echo "--Teardown"
echo "DELETE FROM student_question;"
echo "DELETE FROM options where content = '${OPTION_CONTENT}';"
echo "DELETE FROM topics_questions where questions_id >= ${BASE_ID} and questions_id <= $((${BASE_ID} + ${total_questions}));"
echo "DELETE FROM questions where id >= ${BASE_ID} and id < $((${BASE_ID} + ${total_questions}));"
echo "DELETE FROM topics where id = ${TOPIC_ID};"


printf "\n---------------------------------------------------\n"

printf "STATISTICS\n\n"
printf "[%3d] Approved without justification\n" ${NUM_ROUNDS}
printf "[%3d] Rejected with justification\n" ${NUM_ROUNDS}
printf "[%3d] Waiting for approval\n" ${NUM_ROUNDS}
printf "[%3d] Total Questions\n" ${total_questions}

