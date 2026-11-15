package com.animalloo.ui.match;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;

import com.animalloo.R;
import com.animalloo.databinding.ActivityPetMbtiBinding;
import com.google.android.material.snackbar.Snackbar;

/**
 * Lifestyle-based pet recommendation. Questions are adapted from the supplied
 * reference test, while recommendations remain guidance rather than adoption advice.
 */
public class PetMatchActivity extends AppCompatActivity {

    private static final Question[] QUESTIONS = new Question[]{
            new Question("가장 만족스러운 휴일은 어떤 모습인가요?", new String[]{
                    "상쾌한 아침, 야외에서 운동하기", "집에서 함께 쉬며 영화 보기",
                    "조용한 카페나 공원에서 산책", "집안일을 정리하며 여유 보내기"}, new int[]{0, 1, 2, 3}),
            new Question("현재 주로 어떤 환경에서 생활하나요?", new String[]{
                    "공원이나 산책로가 가까운 곳", "도심 아파트·빌라·오피스텔",
                    "조용한 주택가", "공간이 넉넉하지 않은 실내 중심 환경"}, new int[]{0, 1, 2, 3}),
            new Question("요즘 나의 생활은 얼마나 바쁜가요?", new String[]{
                    "규칙적으로 시간을 낼 수 있다", "바쁘지만 매일 돌봄 시간은 있다",
                    "혼자 있는 시간이 많아 독립적인 친구가 좋다", "현재는 매우 바쁘다"}, new int[]{0, 1, 2, 3}),
            new Question("함께 지내고 싶은 반려동물의 모습은?", new String[]{
                    "밖에서 함께 뛰고 활동하는 친구", "옆에 붙어 교감하고 노는 친구",
                    "각자 편안하게 시간을 보내는 친구", "조용하고 돌봄 부담이 적은 친구"}, new int[]{0, 1, 2, 3}),
            new Question("집을 비울 때 어떤 돌봄이 가능한가요?", new String[]{
                    "산책과 놀이 시간을 충분히 조정할 수 있다", "가족과 번갈아 돌볼 수 있다",
                    "혼자서도 비교적 잘 지내는 성향이 필요하다", "짧은 돌봄 루틴이 현실적이다"}, new int[]{0, 1, 2, 3}),
            new Question("낯선 사람이나 다른 동물을 만나는 상황은?", new String[]{
                    "함께 활동하며 사회화하는 것이 좋다", "사람과의 친밀한 교감을 우선한다",
                    "낯선 환경에 과한 자극은 피하고 싶다", "조용하고 예측 가능한 환경이 좋다"}, new int[]{0, 1, 2, 3}),
            new Question("여행이나 외출 계획은 어떤 편인가요?", new String[]{
                    "반려동물과 함께하는 야외 활동을 자주 하고 싶다", "가까운 곳에 함께 다니고 싶다",
                    "집에서 안정적으로 지내는 편이 좋다", "계획이 자주 바뀌어 돌봄 계획이 어렵다"}, new int[]{0, 1, 2, 3}),
            new Question("반려동물을 맞이하려는 가장 큰 이유는 무엇인가요?", new String[]{
                    "활동적인 일상을 함께하고 싶어서", "정서적으로 교감할 친구가 필요해서",
                    "편안한 동거와 각자의 시간을 존중하고 싶어서", "처음이라 차분하게 돌봄을 시작하고 싶어서"}, new int[]{0, 1, 2, 3})
    };

    private ActivityPetMbtiBinding binding;
    private final int[] scores = new int[4];
    private int questionIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPetMbtiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.tvMbtiTitle.setText(R.string.pet_match_title);
        binding.tvMbtiIntro.setText(R.string.pet_match_intro);
        binding.tvMbtiResultTitle.setText(R.string.pet_match_result_title);
        binding.btnMbtiBack.setOnClickListener(v -> finish());
        binding.btnMbtiNext.setOnClickListener(v -> submitAnswer());
        renderQuestion();
    }

    private void renderQuestion() {
        Question question = QUESTIONS[questionIndex];
        binding.progressMbti.setMax(QUESTIONS.length);
        binding.progressMbti.setProgress(questionIndex);
        binding.tvMbtiProgress.setText(getString(R.string.pet_mbti_progress_format,
                questionIndex + 1, QUESTIONS.length));
        binding.tvMbtiQuestion.setText(question.text);
        binding.groupMbtiAnswers.removeAllViews();
        binding.groupMbtiAnswers.setVisibility(View.VISIBLE);
        for (int index = 0; index < question.answers.length; index++) {
            RadioButton answer = new RadioButton(this);
            answer.setId(View.generateViewId());
            answer.setTag(index);
            answer.setText(question.answers[index]);
            answer.setTextAppearance(this, R.style.TextAppearance_AnimalLoo_Body);
            answer.setPadding(0, 14, 0, 14);
            binding.groupMbtiAnswers.addView(answer);
        }
        binding.btnMbtiNext.setText(questionIndex == QUESTIONS.length - 1
                ? R.string.pet_mbti_result : R.string.pet_mbti_next);
    }

    private void submitAnswer() {
        int checkedId = binding.groupMbtiAnswers.getCheckedRadioButtonId();
        if (checkedId == View.NO_ID) {
            Snackbar.make(binding.getRoot(), R.string.pet_mbti_select_answer, Snackbar.LENGTH_SHORT).show();
            return;
        }
        int answerIndex = (int) binding.groupMbtiAnswers.<RadioButton>findViewById(checkedId).getTag();
        scores[QUESTIONS[questionIndex].resultForAnswer[answerIndex]]++;
        questionIndex++;
        if (questionIndex < QUESTIONS.length) {
            renderQuestion();
        } else {
            showResult();
        }
    }

    private void showResult() {
        int winner = 0;
        for (int index = 1; index < scores.length; index++) {
            if (scores[index] > scores[winner]) winner = index;
        }
        String[] titles = {"활동 파트너형 반려견", "교감 중심형 반려견", "독립적인 고양이", "차분한 초보 보호자형"};
        String[] descriptions = {
                "산책·놀이·훈련을 함께 즐길 수 있는 활동적인 반려견과 잘 맞아요. 매일 충분한 운동 시간과 사회화 계획을 준비해 주세요.",
                "사람과의 교감과 애착 형성을 좋아하는 반려견과 잘 맞아요. 푸들처럼 지능과 교감 욕구가 높은 유형도 후보가 될 수 있어요.",
                "각자의 시간을 존중하며 안정적으로 함께 지내는 고양이와 잘 맞아요. 환경 적응을 위한 숨숨집과 놀이 공간을 마련해 주세요.",
                "처음 돌봄을 시작한다면 생활 리듬과 돌봄 비용을 먼저 점검해 주세요. 성체·임시보호·보호소 상담을 통해 차분한 성향의 동물을 만나는 방법도 좋아요."
        };
        binding.tvMbtiIntro.setVisibility(View.GONE);
        binding.progressMbti.setProgress(QUESTIONS.length);
        binding.tvMbtiProgress.setText(getString(R.string.pet_mbti_progress_format,
                QUESTIONS.length, QUESTIONS.length));
        binding.tvMbtiQuestion.setText(R.string.pet_match_result_title);
        binding.groupMbtiAnswers.setVisibility(View.GONE);
        binding.cardMbtiResult.setVisibility(View.VISIBLE);
        binding.tvMbtiType.setText(titles[winner]);
        binding.tvMbtiDescription.setText(descriptions[winner]);
        binding.btnMbtiNext.setText(R.string.pet_mbti_close);
        binding.btnMbtiNext.setOnClickListener(v -> finish());
    }

    private static class Question {
        final String text;
        final String[] answers;
        final int[] resultForAnswer;

        Question(String text, String[] answers, int[] resultForAnswer) {
            this.text = text;
            this.answers = answers;
            this.resultForAnswer = resultForAnswer;
        }
    }
}
