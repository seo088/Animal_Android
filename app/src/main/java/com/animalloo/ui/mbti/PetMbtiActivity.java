package com.animalloo.ui.mbti;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.animalloo.R;
import com.animalloo.databinding.ActivityPetMbtiBinding;
import com.google.android.material.snackbar.Snackbar;

/** A short, local-only personality test. It deliberately does not present itself as medical advice. */
public class PetMbtiActivity extends AppCompatActivity {

    private static final String PREFS = "pet_mbti";
    private static final String KEY_TYPE = "result_type";
    private static final String KEY_DESCRIPTION = "result_description";

    private static final Question[] QUESTIONS = new Question[]{
            new Question("낯선 사람을 만났을 때 우리 아이는?", "먼저 다가가 인사해요", "거리를 두고 관찰해요", 0),
            new Question("산책이나 놀이 시간이 되면?", "먼저 신나서 표현해요", "부르면 천천히 참여해요", 0),
            new Question("새로운 장소에 가면?", "금방 탐색을 시작해요", "익숙해질 시간이 필요해요", 0),
            new Question("혼자 있는 시간에는?", "사람이나 다른 동물을 찾아요", "혼자만의 시간을 잘 보내요", 1),
            new Question("놀이를 고를 때 더 좋아하는 것은?", "공 던지기·함께하는 놀이", "노즈워크·혼자 푸는 놀이", 1),
            new Question("평소 생활 패턴은?", "정해진 시간과 루틴이 편해요", "새로운 자극과 변화를 좋아해요", 2),
            new Question("간식이나 장난감을 주면?", "익숙한 것을 안정적으로 즐겨요", "새로운 것을 먼저 궁금해해요", 2),
            new Question("보호자의 지시에 대한 반응은?", "차분히 기다리고 따라와요", "호기심이 생기면 먼저 움직여요", 3)
    };

    private ActivityPetMbtiBinding binding;
    private final int[] scores = new int[4];
    private int questionIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPetMbtiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnMbtiBack.setOnClickListener(v -> finish());
        binding.btnMbtiNext.setOnClickListener(v -> submitCurrentAnswer());
        renderQuestion();
    }

    private void renderQuestion() {
        Question question = QUESTIONS[questionIndex];
        binding.cardMbtiResult.setVisibility(View.GONE);
        binding.tvMbtiIntro.setVisibility(View.VISIBLE);
        binding.progressMbti.setProgress(questionIndex);
        binding.tvMbtiProgress.setText(getString(R.string.pet_mbti_progress_format,
                questionIndex + 1, QUESTIONS.length));
        binding.tvMbtiQuestion.setText(question.text);
        binding.groupMbtiAnswers.removeAllViews();
        addAnswer(question.firstAnswer, 0);
        addAnswer(question.secondAnswer, 1);
        binding.btnMbtiNext.setText(questionIndex == QUESTIONS.length - 1
                ? R.string.pet_mbti_result : R.string.pet_mbti_next);
    }

    private void addAnswer(String label, int answerIndex) {
        RadioButton button = new RadioButton(this);
        button.setId(View.generateViewId());
        button.setTag(answerIndex);
        button.setText(label);
        button.setTextAppearance(this, R.style.TextAppearance_AnimalLoo_Body);
        button.setPadding(0, 16, 0, 16);
        binding.groupMbtiAnswers.addView(button);
    }

    private void submitCurrentAnswer() {
        int checkedId = binding.groupMbtiAnswers.getCheckedRadioButtonId();
        if (checkedId == View.NO_ID) {
            Snackbar.make(binding.getRoot(), R.string.pet_mbti_select_answer, Snackbar.LENGTH_SHORT).show();
            return;
        }
        RadioButton answer = binding.groupMbtiAnswers.findViewById(checkedId);
        int answerIndex = (int) answer.getTag();
        Question question = QUESTIONS[questionIndex];
        scores[question.dimension] += answerIndex == 0 ? 1 : -1;
        questionIndex++;
        if (questionIndex < QUESTIONS.length) {
            renderQuestion();
        } else {
            showResult();
        }
    }

    private void showResult() {
        String type = (scores[0] >= 0 ? "E" : "I")
                + (scores[1] >= 0 ? "S" : "D")
                + (scores[2] >= 0 ? "R" : "N")
                + (scores[3] >= 0 ? "C" : "H");
        String description = descriptionFor(type);
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putString(KEY_TYPE, type)
                .putString(KEY_DESCRIPTION, description)
                .apply();

        binding.tvMbtiIntro.setVisibility(View.GONE);
        binding.tvMbtiProgress.setText(getString(R.string.pet_mbti_progress_format,
                QUESTIONS.length, QUESTIONS.length));
        binding.progressMbti.setProgress(QUESTIONS.length);
        binding.tvMbtiQuestion.setText(R.string.pet_mbti_result_title);
        binding.groupMbtiAnswers.setVisibility(View.GONE);
        binding.cardMbtiResult.setVisibility(View.VISIBLE);
        binding.tvMbtiType.setText(type);
        binding.tvMbtiDescription.setText(description);
        binding.btnMbtiNext.setText(R.string.pet_mbti_close);
        binding.btnMbtiNext.setOnClickListener(v -> finish());
    }

    @NonNull
    private String descriptionFor(String type) {
        boolean outgoing = type.charAt(0) == 'E';
        boolean social = type.charAt(1) == 'S';
        boolean routine = type.charAt(2) == 'R';
        boolean calm = type.charAt(3) == 'C';
        return (outgoing ? "호기심이 많고 표현이 풍부한" : "신중하게 주변을 살피는")
                + " 성향이에요. "
                + (social ? "함께하는 놀이와 교감" : "혼자 집중하는 놀이와 충분한 휴식")
                + "이 잘 맞고, "
                + (routine ? "예측 가능한 일상" : "가벼운 새로운 자극")
                + "을 마련해 주세요. "
                + (calm ? "차분한 격려" : "짧고 즐거운 활동")
                + "이 자신감을 키우는 데 도움이 됩니다.";
    }

    private static class Question {
        final String text;
        final String firstAnswer;
        final String secondAnswer;
        final int dimension;

        Question(String text, String firstAnswer, String secondAnswer, int dimension) {
            this.text = text;
            this.firstAnswer = firstAnswer;
            this.secondAnswer = secondAnswer;
            this.dimension = dimension;
        }
    }
}
