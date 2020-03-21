#!/bin/sh

filename="teacherActions.csv"
EVAL=("APPROVED" "REJECTED")

QBASE=10001               # First question ID
NQ=1000                 # Number of questions (same as in SQL generator)
NT=10                   # Number of teachers
NC=5                    # Number of classes
        # 1. Approved with justification
        # 2. Approved without justification
        # 3. Rejected with justification
        # 4. Rejected without justification
        # 5. Keep pending for aproval


if [[ $((${NQ} % ${NT})) -ne 0 ]]; then
    echo "Error: Cannot divide all the questions by all the teachers"
    exit -1
fi
QPT=$((${NQ} / ${NT}))  # questions per teacher

if [[ $((${QPT} % ${NC})) -ne 0 ]]; then
    echo "Error: Cannot divide all the teacher questions by all the question classes"
    exit -1
fi
QPC=$((${QPT} / ${NC})) # questions per class


printf "Using %d questions\n" ${NQ}
printf "Using %d teachers\n" ${NT}
printf "Using %d question classes\n" ${NC}
printf "   -> %d questions per teacher\n" ${QPT}
printf "   -> %d questions per teacher per class\n" ${QPC}
printf "\n---------------------------------------------------\n"

accept=0
reject=0
accept_w_j=0
reject_w_j=0

# Get empty file (no need to remove)
printf "[*] writing into %s... " ${filename}
printf "" > ${filename}

for teacher in `seq 0 $((${NT} - 1))`; do

    for class in `seq 0 $((${NC} - 1))`; do
        # ignore waiting for approval questions
        if [[ "${class}" -eq 4 ]]; then continue; fi

        # ignore reject with no justification
        if [[ "${class}" -eq 3 ]]; then continue; fi

        for question in `seq 0 $((${QPC} - 1))`; do
            qid=$((${QBASE} + ${teacher}*${QPT} + ${class}*${QPC} + ${question}))

            # provide justification?
            just=""
            if [[ "${class}" -lt 2 ]]; then 
                just=$(printf "Q_%d: %s" ${qid} ${EVAL[$((${class} % 2 ))]})
            fi

            echo ${qid},${EVAL[$((${class} % 2 ))]},${just} >> ${filename}

            # statistics
            if [[ $((${class} % 2)) -eq 0 ]]; then
                accept=$((${accept} + 1))
                if [[ "${class}" -lt 2 ]]; then 
                    accept_w_j=$((${accept_w_j} + 1))
                fi
            else
                reject=$((${reject} + 1))
                if [[ "${class}" -lt 2 ]]; then 
                    reject_w_j=$((${reject_w_j} + 1))
                fi

            fi
        done
    done
done
echo "Done."
printf "\n---------------------------------------------------\n"

printf "STATISTICS\n\n"
printf "[%3d] Approved with justification\n" ${accept_w_j}
printf "[%3d] Approved without justification\n" $((${accept} - ${accept_w_j}))
printf "[%3d] Rejected with justification\n" ${reject_w_j}
printf "[%3d] Rejected without justification\n" $((${reject} - ${reject_w_j}))
printf "[%3d] Waiting for approval\n" $((${NQ} - ${accept} - ${reject}))

