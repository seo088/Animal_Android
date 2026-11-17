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
            binding.tvMatchBreedTraitsTitle.setVisibility(View.GONE); binding.tvMatchBreedTraits.setVisibility(View.GONE);
        } else {
            List<BreedScore> results = calculateRecommendations(user); BreedScore first = results.get(0); binding.tvMatchResultType.setText(first.breed.name); binding.tvMatchResultDescription.setText(buildReasons(user, first.breed));
            binding.tvMatchAlternatives.setText(getString(R.string.pet_match_alternatives_format, results.get(1).breed.name, results.get(2).breed.name)); binding.tvMatchResultNote.setText(first.breed.consideration);
            BreedFeatures features = featuresFor(first.breed.name);
            binding.tvMatchBreedTraitsTitle.setVisibility(View.VISIBLE); binding.tvMatchBreedTraits.setVisibility(View.VISIBLE);
            binding.tvMatchBreedTraitsTitle.setText(getString(R.string.pet_match_breed_traits_format, first.breed.name));
            binding.tvMatchBreedTraits.setText(features.bullets.replace("\n", "\n\n"));
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

    private BreedFeatures featuresFor(String breed) {
        switch (breed) {
            case "진돗개": return f("• 가족에게는 한결같이 마음을 주는 든든한 친구예요.\n• 낯선 사람에게는 처음엔 조심스러울 수 있어요.\n• 산책과 야외활동을 함께 즐기면 더 행복해해요.");
            case "래브라도 리트리버": return f("• 사람을 정말 좋아해서 가족과 함께하는 시간을 즐겨요.\n• 새로운 놀이와 훈련을 배우는 것을 좋아해요.\n• 에너지가 많아 매일 신나게 뛰어놀 시간이 필요해요.");
            case "저먼 셰퍼드": return f("• 똑똑해서 보호자가 알려주는 것을 잘 배우는 편이에요.\n• 가족을 지키고 싶어 하는 든든한 성격을 가졌어요.\n• 어릴 때부터 여러 사람과 환경을 경험하게 해주세요.");
            case "골든 리트리버": return f("• 다정하고 밝아서 가족 모두와 잘 어울리는 친구예요.\n• 사람이나 다른 반려동물과 친해지는 것을 좋아해요.\n• 산책과 공놀이처럼 함께하는 활동을 무척 좋아해요.");
            case "시베리안 허스키": return f("• 호기심이 많고 바깥에서 신나게 뛰는 것을 좋아해요.\n• 충분히 놀지 못하면 심심함을 크게 느낄 수 있어요.\n• 활동적인 보호자와 함께할 때 매력이 더 잘 보여요.");
            case "비글": return f("• 여기저기 냄새를 맡으며 탐험하는 것을 좋아해요.\n• 밝고 장난기 많은 분위기로 집에 활기를 더해줘요.\n• 산책할 때 냄새 놀이를 함께해 주면 무척 즐거워해요.");
            case "푸들": return f("• 애교가 많고 보호자 곁에 꼭 붙어 있으려는 친구예요.\n• 눈치가 빠르고 새로운 놀이를 배우는 것을 좋아해요.\n• 복슬복슬한 털을 예쁘게 유지하려면 미용이 필요해요.");
            case "닥스훈트": return f("• 작은 몸으로도 씩씩하게 자기 생각을 표현하는 친구예요.\n• 사람을 좋아하고 가족과 함께하는 시간을 즐겨요.\n• 긴 허리에 부담이 가지 않도록 높은 곳은 조심해 주세요.");
            case "시추": return f("• 품에 안겨 쉬는 시간과 보호자의 관심을 좋아해요.\n• 귀엽고 느긋한 매력으로 실내에서 함께 지내기 좋아요.\n• 긴 털이 엉키지 않도록 빗질을 자주 해주세요.");
            case "치와와": return f("• 작은 몸으로 보호자를 졸졸 따라다니는 귀여운 친구예요.\n• 익숙한 가족에게는 애정 표현이 아주 깊을 수 있어요.\n• 체구가 작으니 안전하게 쉴 수 있는 공간이 중요해요.");
            case "포메라니안": return f("• 작고 풍성한 털 덕분에 늘 사랑스러운 존재감을 보여줘요.\n• 호기심이 많고 자기 가족에게 관심을 받고 싶어 해요.\n• 털을 자주 빗겨 주면 더 건강하고 예쁘게 지낼 수 있어요.");
            case "보스턴 테리어": return f("• 표정이 풍부하고 함께 놀면 분위기를 밝게 만들어줘요.\n• 가족과 가까이 지내는 것을 좋아하는 편이에요.\n• 더운 날에는 무리하게 뛰기보다 시원하게 쉬게 해주세요.");
            case "비숑프리제": return f("• 밝은 성격과 사랑스러운 애교로 기분 좋은 친구가 되어줘요.\n• 사람과 함께 놀고 관심받는 시간을 좋아해요.\n• 폭신한 털을 유지하려면 꾸준한 미용이 필요해요.");
            case "미니어처 핀셔": return f("• 작은 몸이지만 에너지는 아주 넘치는 친구예요.\n• 호기심이 많아 집 안팎을 탐험하는 것을 좋아해요.\n• 짧게라도 매일 놀아주고 산책하는 시간이 필요해요.");
            case "불독": return f("• 느긋하고 다정해서 곁에서 편안히 쉬는 시간을 좋아해요.\n• 겉모습과 달리 보호자의 관심을 무척 좋아하는 편이에요.\n• 무리한 운동보다 가벼운 산책과 체중 관리가 중요해요.");
            case "아메리칸 코커스패니얼": return f("• 사람을 좋아하고 가족에게 살갑게 다가오는 친구예요.\n• 함께 놀고 배우는 시간을 즐기는 편이에요.\n• 귀와 풍성한 털을 꼼꼼히 관리해 주세요.");
            case "웰시 코기 펨브로크": return f("• 짧은 다리로 바쁘게 움직이며 함께 놀기를 좋아해요.\n• 영리해서 보호자와 하는 놀이와 훈련을 즐겨요.\n• 산책만으로 부족할 수 있으니 다양한 활동을 준비해 주세요.");
            case "샤페이": return f("• 처음에는 차분하고 조심스럽게 다가오는 친구예요.\n• 익숙한 가족과는 편안한 관계를 만들어 갈 수 있어요.\n• 피부 주름 사이를 깨끗하게 살펴봐 주는 관심이 필요해요.");
            case "재패니즈 스피츠": return f("• 복슬복슬한 털과 밝은 표정이 매력적인 친구예요.\n• 가족과 함께 지내며 관심받는 시간을 좋아해요.\n• 풍성한 털이 엉키지 않도록 빗질을 꾸준히 해주세요.");
            default: return f("• 개체별 성격과 성장 환경에 따라 특징이 달라질 수 있어요.\n• 실제 만남과 보호소 상담으로 성향을 확인해 주세요.");
        }
    }

    private BreedFeatures f(String bullets) { return new BreedFeatures(bullets); }

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
    private static class BreedFeatures { final String bullets; BreedFeatures(String value){bullets=value;} }
}
