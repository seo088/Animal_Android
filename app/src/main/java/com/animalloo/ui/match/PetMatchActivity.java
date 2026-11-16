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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Eight-question dog breed recommendation based on lifestyle compatibility. */
public class PetMatchActivity extends AppCompatActivity {
    private static final int ACTIVITY = 0, BONDING = 1, INDEPENDENCE = 2, SOCIAL = 3, TRAINING = 4, SPACE = 5, GROOMING = 6;
    private static final float[] WEIGHTS = {1.5f, 1.5f, 1.5f, 1f, 1f, 1.5f, .4f};
    private static final Question[] QUESTIONS = new Question[]{
            q("생활 스타일", "가장 만족스러운 휴일은 어떤 모습인가요?", new String[]{"상쾌한 아침, 야외에서 운동하기", "집에서 함께 쉬며 영화 보기", "조용한 카페나 공원에서 산책", "집안일을 정리하며 여유 보내기"}, new int[][]{{5,3,2,4,3,4,3},{2,5,2,3,2,2,3},{3,4,3,3,2,3,3},{2,3,4,2,2,2,3}}),
            q("생활환경", "현재 주로 어떤 환경에서 생활하나요?", new String[]{"공원이나 산책로가 가까운 곳", "도심 아파트·빌라·오피스텔", "조용한 주택가", "공간이 넉넉하지 않은 실내 중심 환경"}, new int[][]{{4,3,3,4,3,4,3},{3,4,3,3,3,2,3},{3,3,4,2,3,3,3},{2,4,3,2,2,1,3}}),
            q("돌봄 시간", "요즘 나의 생활은 얼마나 바쁜가요?", new String[]{"규칙적으로 시간을 낼 수 있다", "바쁘지만 매일 돌봄 시간은 있다", "혼자 있는 시간이 많아 독립적인 친구가 좋다", "현재는 매우 바쁘다"}, new int[][]{{4,5,2,4,5,3,3},{3,4,3,3,3,2,3},{2,2,5,2,2,2,3},{1,1,5,1,1,1,3}}),
            q("선호 성향", "함께 지내고 싶은 반려동물의 모습은?", new String[]{"밖에서 함께 뛰고 활동하는 친구", "옆에 붙어 교감하고 노는 친구", "각자 편안하게 시간을 보내는 친구", "조용하고 돌봄 부담이 적은 친구"}, new int[][]{{5,4,2,4,4,4,3},{3,5,2,3,3,2,3},{2,2,5,2,2,2,3},{1,2,4,1,1,1,2}}),
            q("돌봄 계획", "집을 비울 때 어떤 돌봄이 가능한가요?", new String[]{"산책과 놀이 시간을 충분히 조정할 수 있다", "가족과 번갈아 돌볼 수 있다", "혼자서도 비교적 잘 지내는 성향이 필요하다", "짧은 돌봄 루틴이 현실적이다"}, new int[][]{{5,5,2,4,4,3,3},{3,4,2,3,3,2,3},{2,2,5,2,2,2,3},{1,2,4,1,1,1,2}}),
            q("사회성", "낯선 사람이나 다른 동물을 만나는 상황은?", new String[]{"함께 활동하며 사회화하는 것이 좋다", "사람과의 친밀한 교감을 우선한다", "낯선 환경에 과한 자극은 피하고 싶다", "조용하고 예측 가능한 환경이 좋다"}, new int[][]{{4,4,2,5,3,3,3},{3,5,2,3,3,2,3},{2,3,4,2,2,2,3},{2,3,4,1,2,2,3}}),
            q("외출 패턴", "여행이나 외출 계획은 어떤 편인가요?", new String[]{"반려동물과 함께하는 야외 활동을 자주 하고 싶다", "가까운 곳에 함께 다니고 싶다", "집에서 안정적으로 지내는 편이 좋다", "계획이 자주 바뀌어 돌봄 계획이 어렵다"}, new int[][]{{5,4,2,4,3,4,3},{3,4,3,3,2,2,3},{2,3,4,2,2,2,3},{1,1,5,1,1,1,3}}),
            q("함께하는 이유", "반려동물을 맞이하려는 가장 큰 이유는 무엇인가요?", new String[]{"활동적인 일상을 함께하고 싶어서", "정서적으로 교감할 친구가 필요해서", "편안한 동거와 각자의 시간을 존중하고 싶어서", "처음이라 차분하게 돌봄을 시작하고 싶어서"}, new int[][]{{5,4,2,4,4,4,3},{3,5,2,3,3,2,3},{2,2,5,2,2,2,3},{2,3,3,2,2,2,3}})
    };
    private ActivityPetMatchBinding binding;
    private final int[] selectedAnswers = new int[QUESTIONS.length];
    private int questionIndex;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state); binding = ActivityPetMatchBinding.inflate(getLayoutInflater()); setContentView(binding.getRoot()); resetAnswers();
        binding.btnMatchBack.setOnClickListener(v -> finish());
        binding.btnMatchPrevious.setOnClickListener(v -> { if (questionIndex > 0) { questionIndex--; renderQuestion(); } });
        binding.btnMatchNext.setOnClickListener(v -> advance()); binding.btnMatchRetry.setOnClickListener(v -> resetTest()); renderQuestion();
    }

    private void renderQuestion() {
        Question question = QUESTIONS[questionIndex]; binding.layoutMatchQuestion.setVisibility(View.VISIBLE); binding.layoutMatchResult.setVisibility(View.GONE);
        binding.btnMatchNext.setVisibility(View.VISIBLE); binding.btnMatchRetry.setVisibility(View.GONE); binding.tvMatchCategory.setText(question.category); binding.tvMatchQuestion.setText(question.text);
        binding.tvMatchProgress.setText(getString(R.string.pet_mbti_progress_format, questionIndex + 1, QUESTIONS.length)); binding.btnMatchPrevious.setVisibility(questionIndex == 0 ? View.INVISIBLE : View.VISIBLE);
        binding.btnMatchNext.setText(questionIndex == QUESTIONS.length - 1 ? R.string.pet_mbti_result : R.string.pet_mbti_next); boolean hasAnswer = selectedAnswers[questionIndex] >= 0;
        binding.btnMatchNext.setEnabled(hasAnswer); binding.btnMatchNext.setAlpha(hasAnswer ? 1f : .45f); renderSteps(); binding.layoutMatchOptions.removeAllViews();
        for (int index = 0; index < question.answers.length; index++) addOption(index, question.answers[index]);
    }

    private void renderSteps() {
        binding.layoutMatchSteps.removeAllViews();
        for (int index = 0; index < QUESTIONS.length; index++) {
            TextView step = new TextView(this); LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(30), 1f); if (index > 0) params.setMarginStart(dp(4));
            step.setLayoutParams(params); step.setGravity(Gravity.CENTER); step.setText(String.valueOf(index + 1)); step.setTextSize(12); boolean reached = index <= questionIndex;
            step.setTextColor(ContextCompat.getColor(this, reached ? R.color.color_on_primary : R.color.color_text_secondary)); step.setBackgroundResource(reached ? R.drawable.bg_match_step_active : R.drawable.bg_match_step_idle); binding.layoutMatchSteps.addView(step);
        }
    }

    private void addOption(int index, String text) {
        RadioButton option = new RadioButton(this); LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(60)); if (index > 0) params.topMargin = dp(10);
        option.setLayoutParams(params); option.setPadding(dp(16), 0, dp(12), 0); option.setText(text); option.setTextSize(15); option.setTextColor(ContextCompat.getColor(this, R.color.color_text_primary)); option.setButtonTintList(ContextCompat.getColorStateList(this, R.color.color_test_primary));
        boolean selected = selectedAnswers[questionIndex] == index; option.setChecked(selected); option.setBackgroundResource(selected ? R.drawable.bg_match_option_selected : R.drawable.bg_match_option);
        option.setOnClickListener(v -> { selectedAnswers[questionIndex] = index; renderQuestion(); }); binding.layoutMatchOptions.addView(option);
    }

    private void advance() { if (selectedAnswers[questionIndex] < 0) return; if (questionIndex < QUESTIONS.length - 1) { questionIndex++; renderQuestion(); } else showResult(); }

    private void showResult() {
        UserProfile user = buildUserProfile(); binding.layoutMatchQuestion.setVisibility(View.GONE); binding.layoutMatchResult.setVisibility(View.VISIBLE); binding.btnMatchPrevious.setVisibility(View.GONE); binding.btnMatchNext.setVisibility(View.GONE); binding.btnMatchRetry.setVisibility(View.VISIBLE);
        if (user.needsPreparation) {
            binding.tvMatchResultType.setText(R.string.pet_match_preparation_title); binding.tvMatchResultDescription.setText(R.string.pet_match_preparation_description); binding.tvMatchAlternatives.setText(R.string.pet_match_preparation_next); binding.tvMatchResultNote.setText(R.string.pet_match_preparation_note);
        } else {
            List<BreedScore> results = calculateRecommendations(user); BreedScore first = results.get(0); binding.tvMatchResultType.setText(first.breed.name); binding.tvMatchResultDescription.setText(buildReasons(user, first.breed));
            binding.tvMatchAlternatives.setText(getString(R.string.pet_match_alternatives_format, results.get(1).breed.name, results.get(2).breed.name)); binding.tvMatchResultNote.setText(first.breed.consideration);
        }
        binding.scrollMatch.post(() -> binding.scrollMatch.fullScroll(View.FOCUS_UP));
    }

    private UserProfile buildUserProfile() {
        float[] totals = new float[7]; for (int question = 0; question < QUESTIONS.length; question++) { int[] values = QUESTIONS[question].answerValues[selectedAnswers[question]]; for (int axis = 0; axis < values.length; axis++) totals[axis] += values[axis]; }
        for (int axis = 0; axis < totals.length; axis++) totals[axis] /= QUESTIONS.length;
        boolean timeWarning = selectedAnswers[2] == 3 || selectedAnswers[6] == 3; boolean conflictingRoutine = selectedAnswers[4] == 3 && (selectedAnswers[0] == 0 || selectedAnswers[3] == 0); return new UserProfile(totals, timeWarning || conflictingRoutine);
    }

    private List<BreedScore> calculateRecommendations(UserProfile user) {
        List<BreedScore> scores = new ArrayList<>();
        for (BreedProfile breed : breedProfiles()) {
            float score = 0; for (int axis = 0; axis < user.values.length; axis++) score += (5f - Math.abs(user.values[axis] - breed.values[axis])) * WEIGHTS[axis];
            if (user.values[SPACE] <= 2.2f && breed.values[SPACE] >= 4) score -= 8; if (user.values[INDEPENDENCE] >= 4f && breed.values[BONDING] >= 5 && breed.values[INDEPENDENCE] <= 2) score -= 8;
            if (user.values[TRAINING] >= 4f && breed.values[TRAINING] >= 4) score += 3; if (user.values[ACTIVITY] >= 4f && breed.values[ACTIVITY] >= 4) score += 3; scores.add(new BreedScore(breed, score));
        }
        Collections.sort(scores, (left, right) -> Float.compare(right.score, left.score)); return scores;
    }

    private String buildReasons(UserProfile user, BreedProfile breed) {
        String[] labels = {"일상 활동량", "보호자와의 교감", "혼자 지내는 시간", "사회화 활동", "훈련 여유", "생활 공간", "털 관리"}; List<Integer> axes = new ArrayList<>();
        for (int axis = 0; axis < user.values.length; axis++) axes.add(axis); Collections.sort(axes, Comparator.comparingDouble(axis -> Math.abs(user.values[axis] - breed.values[axis])));
        return getString(R.string.pet_match_reason_format, labels[axes.get(0)], labels[axes.get(1)], labels[axes.get(2)]);
    }

    private List<BreedProfile> breedProfiles() {
        List<BreedProfile> breeds = new ArrayList<>();
        breeds.add(b("진돗개",4,3,4,2,3,4,2,"낯선 사람·다른 반려동물과의 관계 및 충분한 산책 계획을 확인해 주세요.")); breeds.add(b("래브라도 리트리버",4,5,2,5,4,4,3,"매일 활동과 사람·동물과의 사회화 시간을 확보해 주세요."));
        breeds.add(b("저먼 셰퍼드",4,4,3,3,5,4,3,"일관된 훈련과 사회화, 충분한 활동량이 필요해요.")); breeds.add(b("골든 리트리버",4,5,2,5,4,4,4,"매일 산책·놀이 시간을 꾸준히 확보할 수 있는지 확인해 주세요."));
        breeds.add(b("시베리안 허스키",5,4,2,4,4,5,3,"높은 운동량을 감당할 수 있는 환경과 충분한 활동 계획이 필요해요.")); breeds.add(b("비글",4,4,2,4,3,3,2,"후각 놀이와 꾸준한 산책, 짖음·사회성 관리가 필요해요."));
        breeds.add(b("푸들",4,5,2,4,5,2,5,"정기적인 미용과 빗질, 지적 자극을 위한 놀이가 필요해요.")); breeds.add(b("닥스훈트",3,4,3,3,3,1,3,"짧은 다리와 허리에 무리가 가지 않도록 생활환경을 점검해 주세요."));
        breeds.add(b("시추",2,4,3,2,2,1,5,"얼굴 주변과 긴 털의 정기적인 미용·관리가 필요해요.")); breeds.add(b("치와와",2,4,3,2,2,1,2,"작은 체격을 고려해 안전한 실내 환경과 사회화 계획을 준비해 주세요."));
        breeds.add(b("포메라니안",3,4,3,2,3,1,5,"풍성한 털의 빗질과 짖음·사회화 관리가 필요해요.")); breeds.add(b("보스턴 테리어",3,4,3,3,3,1,2,"더위와 호흡 상태를 고려한 무리하지 않는 활동 계획이 필요해요."));
        breeds.add(b("비숑프리제",3,5,2,4,4,1,5,"사람과의 교감 시간과 꾸준한 미용 계획을 확인해 주세요.")); breeds.add(b("미니어처 핀셔",4,3,3,2,3,1,2,"작은 체격이어도 활동량이 높아 매일 놀이와 산책이 필요해요."));
        breeds.add(b("불독",2,4,3,2,2,2,2,"무리한 운동보다 체중 관리와 적절한 활동량을 확인해 주세요.")); breeds.add(b("아메리칸 코커스패니얼",3,5,2,4,4,2,5,"사람과의 교감, 귀·털 관리 시간을 꾸준히 확보해 주세요."));
        breeds.add(b("웰시 코기 펨브로크",4,4,2,3,4,2,3,"운동과 훈련, 체중 및 허리 부담 관리가 필요해요.")); breeds.add(b("샤페이",3,3,4,2,3,3,2,"독립적인 성향을 존중하고 피부 주름 관리 여부를 확인해 주세요."));
        breeds.add(b("재패니즈 스피츠",3,4,3,3,3,2,4,"털 관리와 규칙적인 산책·사회화 시간을 준비해 주세요.")); return breeds;
    }

    private static Question q(String category, String text, String[] answers, int[][] values) { return new Question(category, text, answers, values); }
    private BreedProfile b(String name,int a,int bond,int independent,int social,int training,int space,int grooming,String note) { return new BreedProfile(name,new int[]{a,bond,independent,social,training,space,grooming},note); }
    private void resetTest() { resetAnswers(); questionIndex = 0; renderQuestion(); binding.scrollMatch.post(() -> binding.scrollMatch.fullScroll(View.FOCUS_UP)); }
    private void resetAnswers() { for (int index = 0; index < selectedAnswers.length; index++) selectedAnswers[index] = -1; }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private static class Question { final String category,text; final String[] answers; final int[][] answerValues; Question(String c,String t,String[] a,int[][] v){category=c;text=t;answers=a;answerValues=v;} }
    private static class UserProfile { final float[] values; final boolean needsPreparation; UserProfile(float[] v,boolean p){values=v;needsPreparation=p;} }
    private static class BreedProfile { final String name,consideration; final int[] values; BreedProfile(String n,int[] v,String c){name=n;values=v;consideration=c;} }
    private static class BreedScore { final BreedProfile breed; final float score; BreedScore(BreedProfile b,float s){breed=b;score=s;} }
}
