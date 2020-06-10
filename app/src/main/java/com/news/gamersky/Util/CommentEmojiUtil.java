package com.news.gamersky.Util;

//https://www.jianshu.com/p/9561bc1f6253

import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;

import androidx.core.content.ContextCompat;

import com.news.gamersky.R;
import com.news.gamersky.ThisApp;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommentEmojiUtil {
    private static Map<String, Integer> emojiMap;
    /**
     * 判断表情的正则表达式 [中英文]
     */
    private static final String EMOJI = "\\[[\\u4e00-\\u9fa5a-zA-Z]+]";

    /**
     * 获取含有表情的spannableString
     *
     * @param commentString 服务器传过来的原始string
     * @return 处理过的string
     */
    public static SpannableString getEmojiString(String commentString) {
        SpannableString spannableString = new SpannableString(Html.fromHtml(commentString));
        Pattern pattern = Pattern.compile(EMOJI);
        Matcher matcher = pattern.matcher(spannableString);
        boolean result = matcher.find();
        //循环直到匹配不到
        while (result) {
            System.out.println(matcher.group());
            if(emojiMap.containsKey(matcher.group())) {
                Drawable drawable = ContextCompat.getDrawable(ThisApp.getContext(), emojiMap.get(matcher.group()));
                drawable.setBounds(0, 0, 75, 75);
                ImageSpan imageSpan = new ImageSpan(drawable);
                //matcher.start()  matcher.end()是匹配到的字符串在原始字符串中的起始位置，进行替换
                spannableString.setSpan(imageSpan, matcher.start(), matcher.end(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
            result = matcher.find();
        }
        return spannableString;
    }

    static {
        emojiMap = new HashMap<>();
        emojiMap.put("[微笑]", R.drawable.emoji001);
        emojiMap.put("[再见]", R.drawable.emoji002);
        emojiMap.put("[呲牙]", R.drawable.emoji003);
        emojiMap.put("[笑哭]", R.drawable.emoji004);
        emojiMap.put("[捂脸哭]", R.drawable.emoji005);
        emojiMap.put("[滑稽]", R.drawable.emoji006);
        emojiMap.put("[抠鼻]", R.drawable.emoji007);
        emojiMap.put("[吃瓜]", R.drawable.emoji008);
        emojiMap.put("[害羞]", R.drawable.emoji009);
        emojiMap.put("[流汗]", R.drawable.emoji010);
        emojiMap.put("[疑问]", R.drawable.emoji011);
        emojiMap.put("[坏笑]", R.drawable.emoji012);
        emojiMap.put("[衰]", R.drawable.emoji013);
        emojiMap.put("[骷髅]", R.drawable.emoji014);
        emojiMap.put("[亲亲]", R.drawable.emoji015);
        emojiMap.put("[色色]", R.drawable.emoji016);
        emojiMap.put("[期待]", R.drawable.emoji017);
        emojiMap.put("[嫌弃]", R.drawable.emoji018);
        emojiMap.put("[沉思]", R.drawable.emoji019);
        emojiMap.put("[柠檬酸]", R.drawable.emoji020);
        emojiMap.put("[无语]", R.drawable.emoji021);
        emojiMap.put("[喷子]", R.drawable.emoji022);
        emojiMap.put("[硬盘]", R.drawable.emoji023);
        emojiMap.put("[狗头]", R.drawable.emoji024);


    }

}
