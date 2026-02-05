package org.jarApiAutomation.data.responseModel.auth.responseBuyGold;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentStatusResponse {

    private boolean success;
    private DataResponse data;

    @Data
    @NoArgsConstructor
    public static class DataResponse {
        private String txnStatus;
        private String header;
        private String title;
        private String description;
        private boolean hasCTA;
        @JsonIgnore private String info;
        @JsonIgnore private String ctaDeeplink;
        private String ctaText;
        private String transactionId;
        private OneTimeInvestOrderDetails oneTimeInvestOrderDetails;
        private WeeklyChallengeResponse weeklyChallengeResponse;
        private boolean showInAppRating;
        private String hvtStatus;
        private String shareText;
        private String shareImageUrl;
        private String paymentProvider;
        private double amount;
        private long transactionDate;
        private String type;
        private String paymentDate;
        private String paymentMethod;
        private String orderId;
        private List<PostPaymentRewardCard> postPaymentRewardCardList;
        private PostOrderCrossSellCard postOrderCrossSellCard;
        private int scrollDelay;
        private Cta cta;
        private boolean firstTransaction;
        private boolean partOfUserStreaks;
        private boolean oneTimeInvestment;
        private boolean retryAllowed;
    }

    @Data
    @NoArgsConstructor
    public static class OneTimeInvestOrderDetails {
        private String name;
        private String phoneNumber;
        private String emailId;
        private String goldVolume;
        private String ratePerGm;
        private double goldAmt;
        private double gstAmt;
        private double totalAmt;
        private Object auspiciousStartTime;
        private Object auspiciousEndTime;
        private Object couponCode;
        private Object couponCodeAmount;
        private Object couponCodeGoldVol;
        private String invoiceLink;
        private String status;
        private boolean auspiciousTime;
    }

    @Data
    @NoArgsConstructor
    public static class WeeklyChallengeResponse {
        private int cardsWon;
        private int uptoRewardAmount;
        private Object challengeId;
        private Object lottieLink;
        private Object description1;
        private String description2;
        private Object banner;
        private boolean challengeCompleted;
    }

    @Data
    @NoArgsConstructor
    public static class PostPaymentRewardCard {
        private String animationType;
        private String deepLink;
        private String bannerText;
        private Object cardsWonTextColor;
        private String cardsWonBgColor;
        private String title;
        private Object secondaryTitle;
        private Object tertiaryTitle;
        private int targetCards;
        private int cardsWon;
        private Object ctaText;
        private Object bannerImage;
        private int daysLeft;
        private Object boundaryColor;
        private List<String> boundaryColorsGradient;
        private Object trophyImage;
        private Object lottieUrl;
        private ProgressInfo progressInfo;
        private String badgeShadowColor;
        private String tagHighlightColor;
    }

    @Data
    @NoArgsConstructor
    public static class ProgressInfo {
        private Object spinsProgressBar;
    }

    @Data
    @NoArgsConstructor
    public static class PostOrderCrossSellCard {
        private String infographicType;
        private Object cardData;
        private String infographicUrl;
        private Object textColor;
        private Object primaryText;
        private Object secondaryText;
        private Object primaryCtaText;
        private Object backgroundColor;
        private String ctaDeeplink;
        private Object ctaBackgroundColor;
        private String cardType;
    }

    @Data
    @NoArgsConstructor
    public static class Cta {
        private String text;
        private String deeplink;
        private Object icon;
        private String message;
    }
}
