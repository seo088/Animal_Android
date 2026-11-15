package com.animalloo.ui.match;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.animalloo.R;
import com.animalloo.databinding.ActivityPetMatchBinding;

/** Guided eight-question lifestyle test; the original questions and scoring are retained. */
public class PetMatchActivity extends AppCompatActivity {
    private static final Question[] QUESTIONS = new Question[]{
            new Question("생활 스타일", "가장 만족스러운 휴일은 어떤 모습인가요?", new String[]{"상쾌한 아침, 야외에서 운동하기", "집에서 함께 쉬며 영화 보기", "조용한 카페나 공원에서 산책", "집안일을 정리하며 여유 보내기"}, new int[]{0, 1, 2, 3}),
            new Question("생활환경", "현재 주로 어떤 환경에서 생활하나요?", new String[]{"공원이나 산책로가 가까운 곳", "도심 아파트·빌라·오피스텔", "조용한 주택가", "공간이 넉넉하지 않은 실내 중심 환경"}, new int[]{0, 1, 2, 3}),
            new Question("돌봄 시간", "요즘 나의 생활은 얼마나 바쁜가요?", new String[]{"규칙적으로 시간을 낼 수 있다", "바쁘지만 매일 돌봄 시간은 있다", "혼자 있는 시간이 많아 독립적인 친구가 좋다", "현재는 매우 바쁘다"}, new int[]{0, 1, 2, 3}),
            new Question("선호 성향", "함께 지내고 싶은 반려동물의 모습은?", new String[]{"밖에서 함께 뛰고 활동하는 친구", "옆에 붙어 교감하고 노는 친구", "각자 편안하게 시간을 보내는 친구", "조용하고 돌봄 부담이 적은 친구"}, new int[]{0, 1, 2, 3}),
            new Question("돌봄 계획", "집을 비울 때 어떤 돌봄이 가능한가요?", new String[]{"산책과 놀이 시간을 충분히 조정할 수 있다", "가족과 번갈아 돌볼 수 있다", "혼자서도 비교적 잘 지내는 성향이 필요하다", "짧은 돌봄 루틴이 현실적이다"}, new int[]{0, 1, 2, 3}),
            new Question("사회성", "낯선 사람이나 다른 동물을 만나는 상황은?", new String[]{"함께 활동하며 사회화하는 것이 좋다", "사람과의 친밀한 교감을 우선한다", "낯선 환경에 과한 자극은 피하고 싶다", "조용하고 예측 가능한 환경이 좋다"}, new int[]{0, 1, 2, 3}),
            new Question("외출 패턴", "여행이나 외출 계획은 어떤 편인가요?", new String[]{"반려동물과 함께하는 야외 활동을 자주 하고 싶다", "가까운 곳에 함께 다니고 싶다", "집에서 안정적으로 지내는 편이 좋다", "계획이 자주 바뀌어 돌봄 계획이 어렵다"}, new int[]{0, 1, 2, 3}),
            new Question("함께하는 이유", "반려동물을 맞이하려는 가장 큰 이유는 무엇인가요?", new String[]{"활동적인 일상을 함께하고 싶어서", "정서적으로 교감할 친구가 필요해서", "편안한 동거와 각자의 시간을 존중하고 싶어서", "처음이라 차분하게 돌봄을 시작하고 싶어서"}, new int[]{0, 1, 2, 3})
    };
    private ActivityPetMatchBinding binding;
    private final int[] selectedAnswers = new int[QUESTIONS.length];
    private int questionIndex;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        binding = ActivityPetMatchBinding.inflate(getLayoutInflater()); setContentView(binding.getRoot());
        for (int i = 0; i < selectedAnswers.length; i++) selectedAnswers[i] = -1;
        binding.btnMatchBack.setOnClickListener(v -> finish());
        binding.btnMatchPrevious.setOnClickListener(v -> { if (questionIndex > 0) { questionIndex--; renderQuestion(); } });
        binding.btnMatchNext.setOnClickListener(v -> advance());
        binding.btnMatchRetry.setOnClickListener(v -> resetTest());
        renderQuestion();
    }

    private void renderQuestion() {
        Question q = QUESTIONS[questionIndex];
        binding.layoutMatchQuestion.setVisibility(View.VISIBLE); binding.layoutMatchResult.setVisibility(View.GONE);
        binding.btnMatchNext.setVisibility(View.VISIBLE); binding.btnMatchRetry.setVisibility(View.GONE);
        binding.tvMatchCategory.setText(q.category); binding.tvMatchQuestion.setText(q.text);
        binding.tvMatchProgress.setText(getString(R.string.pet_mbti_progress_format, questionIndex + 1, QUESTIONS.length));
        binding.btnMatchPrevious.setVisibility(questionIndex == 0 ? View.INVISIBLE : View.VISIBLE);
        binding.btnMatchNext.setText(questionIndex == QUESTIONS.length - 1 ? R.string.pet_mbti_result : R.string.pet_mbti_next);
        boolean hasAnswer = selectedAnswers[questionIndex] >= 0;
        binding.btnMatchNext.setEnabled(hasAnswer);
        binding.btnMatchNext.setAlpha(hasAnswer ? 1f : 0.45f);
        renderSteps(); binding.layoutMatchOptions.removeAllViews();
        for (int i = 0; i < q.answers.length; i++) addOption(i, q.answers[i]);
    }

    private void renderSteps() {
        binding.layoutMatchSteps.removeAllViews();
        for (int i = 0; i < QUESTIONS.length; i++) {
            TextView step = new TextView(this); LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(30), 1f);
            if (i > 0) p.setMarginStart(dp(4)); step.setLayoutParams(p); step.setGravity(Gravity.CENTER); step.setText(String.valueOf(i + 1)); step.setTextSize(12);
            boolean done = i <= questionIndex;
            step.setTextColor(ContextCompat.getColor(this, done ? R.color.color_on_primary : R.color.color_text_secondary));
            step.setBackgroundResource(done ? R.drawable.bg_match_step_active : R.drawable.bg_match_step_idle); binding.layoutMatchSteps.addView(step);
        }
    }

    private void addOption(int index, String text) {
        RadioButton option = new RadioButton(this); LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(60));
        if (index > 0) p.topMargin = dp(10); option.setLayoutParams(p); option.setPadding(dp(16), 0, dp(12), 0); option.setText(text); option.setTextSize(15);
        option.setTextColor(ContextCompat.getColor(this, R.color.color_text_primary)); option.setButtonTintList(ContextCompat.getColorStateList(this, R.color.color_test_primary));
        boolean selected = selectedAnswers[questionIndex] == index; option.setChecked(selected); option.setBackgroundResource(selected ? R.drawable.bg_match_option_selected : R.drawable.bg_match_option);
        option.setOnClickListener(v -> { selectedAnswers[questionIndex] = index; renderQuestion(); }); binding.layoutMatchOptions.addView(option);
    }

    private void advance() { if (selectedAnswers[questionIndex] < 0) return; if (questionIndex < QUESTIONS.length - 1) { questionIndex++; renderQuestion(); } else showResult(); }

    private void showResult() {
        int[] scores = new int[4]; for (int i = 0; i < QUESTIONS.length; i++) scores[QUESTIONS[i].resultForAnswer[selectedAnswers[i]]]++;
        int winner = 0; for (int i = 1; i < scores.length; i++) if (scores[i] > scores[winner]) winner = i;
        String[] titles = {"활동 파트너형 반려견", "교감 중심형 반려견", "독립적인 고양이", "차분한 초보 보호자형"};
        String[] descriptions = {"산책·놀이·훈련을 함께 즐길 수 있는 활동적인 반려견과 잘 맞아요. 매일 충분한 운동 시간과 사회화 계획을 준비해 주세요.", "사람과의 교감과 애착 형성을 좋아하는 반려견과 잘 맞아요. 푸들처럼 지능과 교감 욕구가 높은 유형도 후보가 될 수 있어요.", "각자의 시간을 존중하며 안정적으로 함께 지내는 고양이와 잘 맞아요. 환경 적응을 위한 숨숨집과 놀이 공간을 마련해 주세요.", "처음 돌봄을 시작한다면 생활 리듬과 돌봄 비용을 먼저 점검해 주세요. 성체·임시보호·보호소 상담을 통해 차분한 성향의 동물을 만나는 방법도 좋아요."};
        binding.layoutMatchQuestion.setVisibility(View.GONE); binding.layoutMatchResult.setVisibility(View.VISIBLE); binding.tvMatchResultType.setText(titles[winner]); binding.tvMatchResultDescription.setText(descriptions[winner]);
        binding.btnMatchPrevious.setVisibility(View.GONE); binding.btnMatchNext.setVisibility(View.GONE); binding.btnMatchRetry.setVisibility(View.VISIBLE); binding.scrollMatch.post(() -> binding.scrollMatch.fullScroll(View.FOCUS_UP));
    }

    private void resetTest() { for (int i = 0; i < selectedAnswers.length; i++) selectedAnswers[i] = -1; questionIndex = 0; renderQuestion(); binding.scrollMatch.post(() -> binding.scrollMatch.fullScroll(View.FOCUS_UP)); }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private static class Question { final String category, text; final String[] answers; final int[] resultForAnswer; Question(String category, String text, String[] answers, int[] resultForAnswer) { this.category = category; this.text = text; this.answers = answers; this.resultForAnswer = resultForAnswer; } }
}
