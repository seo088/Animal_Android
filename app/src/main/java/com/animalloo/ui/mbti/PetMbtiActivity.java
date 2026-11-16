package com.animalloo.ui.mbti;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.animalloo.R;
import com.animalloo.databinding.ActivityPetMbtiBinding;
import java.util.HashMap;
import java.util.Map;

/** Twenty-question, local-only dog personality MBTI test. */
public class PetMbtiActivity extends AppCompatActivity {
    private static final String PREFS="pet_mbti", KEY_TYPE="result_type", KEY_DESCRIPTION="result_description";
    private static final Question[] QUESTIONS = new Question[]{
            q("생활 양식","강아지가 가장 좋아하는 간식은 무엇인가요?","모든 간식은 환영!",'P',"특정 간식만 좋아해요",'J'),
            q("에너지 방향","강아지가 갑자기 비 오는 날, 산책하자고 하면?","비 맞아도 괜찮아!",'E',"실내에서 놀자",'I'),
            q("정보 선호","강아지가 가장 신나는 순간은 언제인가요?","새로운 장소를 탐험할 때",'N',"익숙한 공원에서 놀 때",'S'),
            q("에너지 방향","강아지는 다른 강아지들과 어떻게 인사하나요?","누구든 환영! 활발하게 다가간다",'E',"조용히 관찰한 후 접근한다",'I'),
            q("에너지 방향","강아지가 혼자 있을 때 어떤가요?","혼자도 잘 논다",'I',"심심해서 사람을 찾는다",'E'),
            q("정보 선호","강아지가 새 장난감을 받았을 때?","호기심 폭발! 바로 탐색한다",'N',"익숙한 장난감이 더 좋아",'S'),
            q("반응 방식","강아지가 잘못했을 때 보이는 반응은?","미안한 표정으로 감정을 표현한다",'F',"상황을 파악하려는 듯한 표정",'T'),
            q("정보 선호","산책 중 갑자기 낯선 소리를 들었을 때?","경계하고 멈춘다",'S',"호기심에 소리 쪽으로 다가간다",'N'),
            q("에너지 방향","강아지가 새로운 사람을 만났을 때?","신나서 바로 다가간다",'E',"조금 지켜본 후 다가간다",'I'),
            q("정보 선호","강아지가 혼자 놀이를 즐길 때 선호하는 것은?","익숙한 장난감으로 놀기",'S',"새로운 물건 탐색하기",'N'),
            q("반응 방식","강아지가 주인의 기분을 알아차릴 때?","감정에 민감하게 반응한다",'F',"상황을 논리적으로 관찰한다",'T'),
            q("정보 선호","강아지가 새로운 장소에 갔을 때?","여기저기 탐색한다",'N',"안전한 곳에 머문다",'S'),
            q("에너지 방향","강아지가 산책 중 고양이를 봤을 때?","신나서 달려간다",'E',"조심스럽게 지켜본다",'I'),
            q("생활 양식","강아지가 좋아하는 하루 일정은?","규칙적인 일과",'J',"즉흥적인 모험",'P'),
            q("생활 양식","강아지가 새로운 명령어를 배울 때?","빠르게 배우며 창의적으로 응용한다",'P',"반복 학습으로 확실히 익힌다",'J'),
            q("반응 방식","강아지가 주인과 함께 있을 때 더 좋아하는 활동은?","애정 표현하기",'F',"문제 해결 놀이하기",'T'),
            q("생활 양식","강아지가 먹는 것을 고를 때?","그날 기분에 따라 선택",'P',"항상 같은 음식 선호",'J'),
            q("에너지 방향","강아지가 새로운 친구를 사귈 때?","즉시 친해진다",'E',"시간을 두고 친해진다",'I'),
            q("정보 선호","강아지가 산책 중 선택하는 길은?","새로운 길을 가본다",'N',"익숙한 길을 선호한다",'S'),
            q("정보 선호","강아지가 혼자 있을 때 행동은?","조용히 쉬기",'S',"주변 탐색하기",'N')
    };
    private ActivityPetMbtiBinding binding; private final int[] selectedAnswers=new int[QUESTIONS.length]; private int questionIndex;

    @Override public void onCreate(Bundle state){super.onCreate(state);binding=ActivityPetMbtiBinding.inflate(getLayoutInflater());setContentView(binding.getRoot());resetAnswers();binding.btnMbtiBack.setOnClickListener(v->finish());binding.btnMbtiPrevious.setOnClickListener(v->{if(questionIndex>0){questionIndex--;renderQuestion();}});binding.btnMbtiNext.setOnClickListener(v->advance());binding.btnMbtiRetry.setOnClickListener(v->resetTest());renderQuestion();}
    private void renderQuestion(){Question q=QUESTIONS[questionIndex];binding.layoutMbtiQuestion.setVisibility(View.VISIBLE);binding.layoutMbtiResult.setVisibility(View.GONE);binding.btnMbtiNext.setVisibility(View.VISIBLE);binding.btnMbtiRetry.setVisibility(View.GONE);binding.tvMbtiCategory.setText(q.category);binding.tvMbtiQuestion.setText(q.text);binding.tvMbtiProgress.setText(getString(R.string.pet_mbti_progress_format,questionIndex+1,QUESTIONS.length));binding.btnMbtiPrevious.setVisibility(questionIndex==0?View.INVISIBLE:View.VISIBLE);binding.btnMbtiNext.setText(questionIndex==QUESTIONS.length-1?R.string.pet_mbti_result:R.string.pet_mbti_next);boolean answered=selectedAnswers[questionIndex]>=0;binding.btnMbtiNext.setEnabled(answered);binding.btnMbtiNext.setAlpha(answered?1f:.45f);renderSteps();binding.layoutMbtiOptions.removeAllViews();addOption(0,q.first);addOption(1,q.second);}
    private void renderSteps(){binding.layoutMbtiSteps.removeAllViews();for(int i=0;i<QUESTIONS.length;i++){TextView step=new TextView(this);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(8),1f);if(i>0)p.setMarginStart(dp(2));step.setLayoutParams(p);step.setBackgroundResource(i<=questionIndex?R.drawable.bg_match_step_active:R.drawable.bg_match_step_idle);binding.layoutMbtiSteps.addView(step);}}
    private void addOption(int index,String label){RadioButton option=new RadioButton(this);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,dp(64));if(index>0)p.topMargin=dp(10);option.setLayoutParams(p);option.setPadding(dp(16),0,dp(12),0);option.setText(label);option.setTextSize(15);option.setTextColor(ContextCompat.getColor(this,R.color.color_text_primary));option.setButtonTintList(ContextCompat.getColorStateList(this,R.color.color_test_primary));boolean selected=selectedAnswers[questionIndex]==index;option.setChecked(selected);option.setBackgroundResource(selected?R.drawable.bg_match_option_selected:R.drawable.bg_match_option);option.setOnClickListener(v->{selectedAnswers[questionIndex]=index;renderQuestion();});binding.layoutMbtiOptions.addView(option);}
    private void advance(){if(selectedAnswers[questionIndex]<0)return;if(questionIndex<QUESTIONS.length-1){questionIndex++;renderQuestion();}else showResult();}
    private void showResult(){String type=calculateType();Result result=results().get(type);getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString(KEY_TYPE,type).putString(KEY_DESCRIPTION,result.text).apply();binding.layoutMbtiQuestion.setVisibility(View.GONE);binding.layoutMbtiResult.setVisibility(View.VISIBLE);binding.btnMbtiPrevious.setVisibility(View.GONE);binding.btnMbtiNext.setVisibility(View.GONE);binding.btnMbtiRetry.setVisibility(View.VISIBLE);binding.tvMbtiType.setText(type);binding.tvMbtiNickname.setText(result.nickname);binding.tvMbtiDescription.setText(result.text);binding.scrollMbti.post(()->binding.scrollMbti.fullScroll(View.FOCUS_UP));}
    private String calculateType(){Map<Character,Integer> score=new HashMap<>();for(int i=0;i<QUESTIONS.length;i++){Question q=QUESTIONS[i];char c=selectedAnswers[i]==0?q.firstType:q.secondType;score.put(c,score.containsKey(c)?score.get(c)+1:1);}return higher(score,'E','I')+higher(score,'S','N')+higher(score,'T','F')+higher(score,'J','P');}
    private String higher(Map<Character,Integer> scores,char positive,char negative){int a=scores.containsKey(positive)?scores.get(positive):0;int b=scores.containsKey(negative)?scores.get(negative):0;return String.valueOf(a>b?positive:negative);}
    private Map<String,Result> results(){Map<String,Result> r=new HashMap<>();put(r,"ISTJ","세상 속 조용한 모범생","익숙한 일상을 편안해하고, 차분하게 자기 페이스를 지켜가는 친구예요.\n\n정해진 산책과 놀이 루틴이 있으면 더 안정감을 느껴요.");put(r,"ISFJ","임금 뒷편의 권력형","가족을 세심하게 살피고 조용히 곁을 지켜주는 다정한 친구예요.\n\n믿을 수 있는 보호자와의 안정적인 시간이 중요해요.");put(r,"INFJ","예언예언자형","주변 분위기를 잘 읽고 자기만의 깊은 생각을 하는 친구예요.\n\n낯선 자극보다는 편안한 공간에서 마음을 열어요.");put(r,"INTJ","과학자형","관찰한 뒤 자기만의 방법으로 문제를 풀어가는 똑똑한 친구예요.\n\n혼자 집중할 수 있는 놀이도 함께 준비해 주세요.");put(r,"ISTP","백과사전형","조용하지만 호기심이 생기면 직접 확인해 보는 탐험가예요.\n\n부담 없는 노즈워크나 탐색 놀이를 좋아할 수 있어요.");put(r,"ISFP","성인 군자형","부드럽고 평화로운 분위기를 좋아하는 다정한 친구예요.\n\n보호자의 차분한 관심과 편안한 산책을 즐겨요.");put(r,"INFP","산다르크형","섬세한 마음으로 자기만의 세계를 즐기는 친구예요.\n\n재촉하기보다 충분한 시간을 주면 더 편안해해요.");put(r,"INTP","아이디어형","새로운 물건과 방법을 유심히 살피는 호기심 많은 친구예요.\n\n생각하고 탐색할 수 있는 장난감이 잘 맞아요.");put(r,"ESTP","수완 좋은 활동가형","새로운 자극이 보이면 먼저 움직이고 즐기는 에너지 넘치는 친구예요.\n\n다양한 산책 코스와 활동을 좋아해요.");put(r,"ESFP","사교적인 유형","사람과 친구를 만나면 기분이 좋아지는 사랑스러운 친구예요.\n\n함께 놀고 관심받는 시간이 큰 즐거움이에요.");put(r,"ENFP","스파크형","호기심과 상상력이 넘쳐 매일 새로운 재미를 찾는 친구예요.\n\n안전한 범위에서 다양한 경험을 만들어 주세요.");put(r,"ENTP","발명가형","낯선 물건도 재미있는 놀이로 바꾸는 창의적인 친구예요.\n\n머리를 쓰는 문제 해결 놀이를 좋아할 수 있어요.");put(r,"ESTJ","사업가형","활기차고 자신감 있게 일상을 이끄는 친구예요.\n\n규칙적인 활동과 분명한 칭찬을 함께해 주세요.");put(r,"ESFJ","친선도모형","가족의 기분과 분위기에 민감한 다정한 친구예요.\n\n함께하는 산책과 애정 표현에서 행복을 느껴요.");put(r,"ENFJ","언변 능수형","사람과의 교감을 좋아하고 곁에 있는 이들을 잘 챙기는 친구예요.\n\n보호자와 함께 배우고 노는 시간을 즐겨요.");put(r,"ENTJ","지도자형","호기심과 자신감으로 먼저 길을 찾아가는 당찬 친구예요.\n\n활동적인 놀이와 일관된 훈련이 잘 맞아요.");return r;}
    private void put(Map<String,Result> map,String type,String nickname,String text){map.put(type,new Result(nickname,text));}
    private void resetTest(){resetAnswers();questionIndex=0;renderQuestion();binding.scrollMbti.post(()->binding.scrollMbti.fullScroll(View.FOCUS_UP));} private void resetAnswers(){for(int i=0;i<selectedAnswers.length;i++)selectedAnswers[i]=-1;}private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}private static Question q(String c,String t,String a,char x,String b,char y){return new Question(c,t,a,x,b,y);}private static class Question{final String category,text,first,second;final char firstType,secondType;Question(String c,String t,String a,char x,String b,char y){category=c;text=t;first=a;firstType=x;second=b;secondType=y;}}private static class Result{final String nickname,text;Result(String n,String t){nickname=n;text=t;}}
}
