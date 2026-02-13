package com.ticket.api.interceptor;

import com.ticket.api.annotation.QueuePass;
import com.ticket.api.service.QueueService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueInterceptor implements HandlerInterceptor {

    private final QueueService queueService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // ?붿껌??泥섎━??硫붿꽌?쒓? HandlerMethod?몄? ?뺤씤
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 硫붿꽌?쒖뿉 @QueuePass ?대끂?뚯씠?섏씠 遺숈뼱?덈뒗吏 ?뺤씤
        QueuePass queuePass = handlerMethod.getMethodAnnotation(QueuePass.class);
        if (queuePass == null) {
            return true; // ?대끂?뚯씠???놁쑝硫?洹몃깷 ?듦낵
        }

        // ?꾪꽣瑜??대? ?듦낵?덉쑝誘濡?SecurityContext???몄쬆 ?뺣낫媛 ?덉쓬
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("?몄쬆 ?뺣낫媛 ?놁뒿?덈떎.");
        }

        String email = authentication.getName();
        log.info("?湲곗뿴 ?듦낵 寃利??쒖옉 - User: {}", email);

        // ?湲곗뿴 寃利?(?낆옣沅??놁쑝硫??먮윭)
        if (!queueService.isAllowed(email)) {
            log.warn("?湲곗뿴 誘명넻怨??좎? ?묎렐 李⑤떒 - User: {}", email);
            throw new IllegalStateException("?湲곗뿴???듦낵?섏? ?딆? ?좎??낅땲?? 以꾩쓣 ?쒖＜?몄슂.");
        }

        return true; // 寃???듦낵 -> 而⑦듃濡ㅻ윭 ?ㅽ뻾
    }
}
