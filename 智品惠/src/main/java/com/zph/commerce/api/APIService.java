package com.zph.commerce.api;


import com.zph.commerce.bean.Address;
import com.zph.commerce.bean.AfterSaleGoodsInfo;
import com.zph.commerce.bean.AreaBean;
import com.zph.commerce.bean.BankInfo;
import com.zph.commerce.bean.BannerBase;
import com.zph.commerce.bean.BaseResponse;
import com.zph.commerce.bean.BindCard;
import com.zph.commerce.bean.CartGoodsBase;
import com.zph.commerce.bean.CityBean;
import com.zph.commerce.bean.CommentContent;
import com.zph.commerce.bean.ExchangeBean;
import com.zph.commerce.bean.ExchangeInfo;
import com.zph.commerce.bean.Catergory;
import com.zph.commerce.bean.OrderCartBase;
import com.zph.commerce.bean.OrderDetals;
import com.zph.commerce.bean.OrderInfo;
import com.zph.commerce.bean.OrderMailInfo;
import com.zph.commerce.bean.OrderPay;
import com.zph.commerce.bean.ParentCatergory;
import com.zph.commerce.bean.PayMethod;
import com.zph.commerce.bean.PersonAuth;
import com.zph.commerce.bean.PraiseInfo;
import com.zph.commerce.bean.ProvinceBean;
import com.zph.commerce.bean.RecomFriendBase;
import com.zph.commerce.bean.RecommendInfo;
import com.zph.commerce.bean.Shipping;
import com.zph.commerce.bean.UserBean;
import com.zph.commerce.bean.WisdomOrderDetals;
import com.zph.commerce.bean.WxPayResult;
import com.zph.commerce.model.AmountDetail;
import com.zph.commerce.model.GoodsBean;
import com.zph.commerce.model.IntegralPoint;
import com.zph.commerce.model.Inventory;
import com.zph.commerce.model.InventoryDetails;
import com.zph.commerce.model.Message;
import com.zph.commerce.model.MessageBase;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;


public interface APIService {

    /**
     * 登陆
     */
    @GET("login/validateCredentials")
    Call<BaseResponse<String>> loginRepo(@Header("X-AUTH-TOKEN") String token);


    /**
     * 注册
     */
    @POST("registerUser/doRegister")
    @FormUrlEncoded
    Call<BaseResponse<String>> registerRepo(@Field("mobile") String mobile, @Field("vcode") String vcode, @Field("password") String password, @Field("referer") String referer);


    /**
     * 注册:获取验证码
     */
    @POST("registerUser/sendSMS")
    @FormUrlEncoded
    Call<BaseResponse<String>> sendSMS(@Field("mobile") String mobile);

    /**
     * 忘记密码:获取验证码
     */
    @POST("forgetSecret/getCode")
    @FormUrlEncoded
    Call<BaseResponse<String>> getCode(@Field("mobile") String mobile);

    /**
     * 忘记密码:修改密码
     */
    @POST("forgetSecret/inputPassword")
    @FormUrlEncoded
    Call<BaseResponse<String>> resetPwd(@Field("mobile") String mobile, @Field("code") String code, @Field("password") String password);


    /**
     * 支付密码:获取验证码
     */
    @POST("api/sendSMSForPwd")
    @FormUrlEncoded
    Call<BaseResponse<Object>> sendSMSForPwd(@Header("X-AUTH-TOKEN") String token, @Field("mobile") String mobile);


    /**
     * 支付密码:修改、设置支付密码
     */
    @POST("api/alterPwd")
    @FormUrlEncoded
    Call<BaseResponse<Object>> resetPayPwd(@Header("X-AUTH-TOKEN") String token, @Field("mobile") String mobile, @Field("code") String code, @Field("password") String password);


    /**
     * 鼓励积分兑换记录
     *
     * @param token
     * @return
     */
    @POST("api/zphExchangeLog")
    Call<BaseResponse<List<ExchangeBean>>> getExchangeList(@Header("X-AUTH-TOKEN") String token);


    /**
     * 货款兑换记录
     *
     * @param token
     * @return
     */
    @POST("api/zphExchangeLogSurplus")
    Call<BaseResponse<List<ExchangeBean>>> zphExchangeLogSurplus(@Header("X-AUTH-TOKEN") String token);


    /**
     * 货款返还记录
     *
     * @param token
     * @return
     */
    @FormUrlEncoded
    @POST("api/getSurplusMoneyLogByStatus")
    Call<BaseResponse<List<PraiseInfo>>> surplusMoneyLog(@Header("X-AUTH-TOKEN") String token, @Field("status") int status);


    /**
     * 鼓励积分奖励记录
     *
     * @param token
     * @return
     */
    @POST("api/getEncouragePointsLogByStatus")
    @FormUrlEncoded
    Call<BaseResponse<List<PraiseInfo>>> encouragePointsLog(@Header("X-AUTH-TOKEN") String token, @Field("status") int status);


    /**
     * 用户信息
     *
     * @param token
     * @return
     */
    @POST("api/userInformation")
    Call<BaseResponse<List<UserBean>>> getUserInformation(@Header("X-AUTH-TOKEN") String token);

    /**
     * 是否设置了支付密码
     *
     * @param token
     * @return
     */
    @POST("api/getSecret")
    Call<BaseResponse<Object>> getSecret(@Header("X-AUTH-TOKEN") String token);

    /**
     * 积分兑换页面数据
     *
     * @param token
     * @return
     */
    @POST("api/exchangeInfo")
    Call<BaseResponse<List<ExchangeInfo>>> exchangeInfo(@Header("X-AUTH-TOKEN") String token);


    /**
     * 货款兑换页面数据
     *
     * @param token
     * @return
     */
    @POST("api/getExchangeInfoSurplus")
    Call<BaseResponse<List<ExchangeInfo>>> getExchangeInfoSurplus(@Header("X-AUTH-TOKEN") String token);


    /**
     * 收货地址列表
     *
     * @param token
     * @return
     */
    @POST("api/deliveryAdrList")
    Call<BaseResponse<List<Address>>> getAcceptAddressRepo(@Header("X-AUTH-TOKEN") String token);


    /**
     * 删除收货地址
     *
     * @param token
     * @param addr_id
     * @return
     */
    @POST("api/deliveryAdrDel")
    @FormUrlEncoded
    Call<BaseResponse<Object>> deliveryAdrDel(@Header("X-AUTH-TOKEN") String token, @Field("addr_id") String addr_id);

    /**
     * 设置默认地址
     *
     * @param token
     * @param addr_id
     * @return
     */
    @POST("api/deliveryAdrDefault")
    @FormUrlEncoded
    Call<BaseResponse<Object>> deliveryAdrDefault(@Header("X-AUTH-TOKEN") String token, @Field("addr_id") String addr_id);


    /**
     * 修改收货地址
     *
     * @param params
     * @return
     */
    @POST("api/deliveryAdrEdit")
    @FormUrlEncoded
    Call<BaseResponse<Object>> updateAddrRepo(@Header("X-AUTH-TOKEN") String token, @FieldMap Map<String, String> params);


    /**
     * 新增收货地址
     *
     * @param params
     * @return
     */
    @POST("api/deliveryAdrAdd")
    @FormUrlEncoded
    Call<BaseResponse<Object>> addAddrRepo(@Header("X-AUTH-TOKEN") String token, @FieldMap Map<String, String> params);


    /**
     * 获取省信息
     */
    @POST("common/getProviceList")
    Call<BaseResponse<List<ProvinceBean>>> getProivinceList();


    /**
     * 获取市信息
     */
    @POST("common/getCity")
    @FormUrlEncoded
    Call<BaseResponse<List<CityBean>>> getCityList(@Field("region_id") String region_id);


    /**
     * 获取区信息
     */
    @POST("common/getRegion")
    @FormUrlEncoded
    Call<BaseResponse<List<AreaBean>>> getAreaList(@Field("region_id") String region_id);

    /**
     * 获取银行卡列表
     */
    @POST("common/getBankList")
    Call<BaseResponse<List<BindCard>>> getBankLst();


    /**
     * 绑卡:获取验证码
     */
    @POST("api/sendSMS")
    @FormUrlEncoded
    Call<BaseResponse<Object>> getBindCode(@Header("X-AUTH-TOKEN") String token, @Field("mobile") String mobile);


    /**
     * 实名认证
     */
    @POST("api/personAuth")
    @FormUrlEncoded
    Call<BaseResponse<Object>> bindBankSubmit(@Header("X-AUTH-TOKEN") String token,
                                              @Field("name") String name,
                                              @Field("card_number") String card_number,
                                              @Field("hand_logo") String hand_logo,
                                              @Field("front_card") String front_card,
                                              @Field("rear_card") String rear_card,
                                              @Field("bank_id") String bank_id,
                                              @Field("bank_card") String bank_card,
                                              @Field("province") String province,
                                              @Field("city") String city,
                                              @Field("county") String county,
                                              @Field("branch") String branch,
                                              @Field("bank_logo") String bank_logo,
                                              @Field("code") String code);


    /**
     * 查询实名认证
     *
     * @return
     */
    @POST("api/getPersonAuth")
    Call<BaseResponse<List<PersonAuth>>> getPersonAuth(@Header("X-AUTH-TOKEN") String token);

    /**
     * 修改实名认证
     *
     * @return
     */
    @POST("api/updateAuthentication")
    @FormUrlEncoded
    Call<BaseResponse<Object>> updateAuthentication(@Header("X-AUTH-TOKEN") String token, @FieldMap Map<String, String> params);

    /**
     * 获取银行卡信息
     *
     * @param token
     * @return
     */
    @POST("api/changeBank")
    Call<BaseResponse<List<BankInfo>>> getBankInfo(@Header("X-AUTH-TOKEN") String token);

    /**
     * 修改银行卡信息
     *
     * @param token
     * @param params
     * @return
     */
    @POST("api/updateBank")
    @FormUrlEncoded
    Call<BaseResponse<Object>> updateBank(@Header("X-AUTH-TOKEN") String token, @FieldMap Map<String, String> params);


    @POST("common/getBanner")
    Call<BaseResponse<BannerBase>> getBanner();

    /**
     * 获取首页商品列表
     */
    @POST("common/getMall")
    Call<BaseResponse<List<GoodsBean>>> getMall();

    /**
     * 获取商品详情
     */
    @POST("common/getGoodDetail")
    @FormUrlEncoded
    Call<BaseResponse<List<GoodsBean>>> getGoodDetail(@Field("good_id") String good_id);


    /**
     * 普通商品生成确认订单
     */
    @POST("api/generateOrder")
    @FormUrlEncoded
    Call<BaseResponse<List<OrderCartBase>>> generateOrder(@Header("X-AUTH-TOKEN") String token,
                                                          @Field("goods_type") int goods_type,
                                                          @Field("goods_attr_id") String goods_attr_id,
                                                          @Field("queryId") String queryId,
                                                          @Field("addr_id") String addr_id);


    /**
     * 小哈生成确认订单
     */
    @POST("api/xhGenerateOrder1")
    @FormUrlEncoded
    Call<BaseResponse<List<OrderCartBase>>> xhGenerateOrder(@Header("X-AUTH-TOKEN") String token,
                                                            @Field("goods_type") int goods_type,
                                                            @Field("goods_attr_id") String goods_attr_id,
                                                            @Field("number") String number,
                                                            @Field("addr_id") String addr_id);


    /**
     * 小哈生成订单
     */
    @POST("api/submitOrder1")
    @FormUrlEncoded
    Call<BaseResponse<List<OrderPay>>> submitOrder(@Header("X-AUTH-TOKEN") String token, @FieldMap Map<String, Object> params);


    /**
     * 普通商品生成订单
     */
    @POST("api/submitOrderGoods")
    @FormUrlEncoded
    Call<BaseResponse<List<OrderPay>>> submitOrderGoods(@Header("X-AUTH-TOKEN") String token, @FieldMap Map<String, Object> params);

    /**
     * 获取支付方式控制
     *
     * @return
     */
    @POST("common/payrole")
    Call<BaseResponse<List<PayMethod>>> getPayMethod();


    /**
     * 支付宝支付
     */
    @POST("api/getAlipayOrder")
    @FormUrlEncoded
    Call<BaseResponse<List<String>>> getAlipayOrder(@Header("X-AUTH-TOKEN") String token, @Field("trade_id") String trade_id);

    /**
     * 微信支付
     */
    @POST("api/getWXOrder")
    @FormUrlEncoded
    Call<BaseResponse<List<WxPayResult>>> getWXOrder(@Header("X-AUTH-TOKEN") String token, @Field("trade_id") String trade_id, @Field("type") int type);

    /**
     * 银联支付
     */
    @POST("api/getUnionOrder")
    @FormUrlEncoded
    Call<BaseResponse<List<String>>> getUnionOrder(@Header("X-AUTH-TOKEN") String token, @Field("trade_id") String trade_id);


    /**
     * 提货支付宝支付
     */
    @POST("api/deliveryAlipayOrder")
    @FormUrlEncoded
    Call<BaseResponse<List<String>>> deliveryAlipayOrder(@Header("X-AUTH-TOKEN") String token, @Field("trade_id") String trade_id);

    /**
     * 提货微信支付
     */
    @POST("api/deliveryGetWXOrder")
    @FormUrlEncoded
    Call<BaseResponse<List<WxPayResult>>> deliveryGetWXOrder(@Header("X-AUTH-TOKEN") String token, @Field("trade_id") String trade_id, @Field("type") int type);

    /**
     * 提货银联支付
     */
    @POST("api/deliveryUnionOrder")
    @FormUrlEncoded
    Call<BaseResponse<List<String>>> deliveryUnionOrder(@Header("X-AUTH-TOKEN") String token, @Field("trade_id") String trade_id);


    /**
     * 鼓励积分支付
     */
    @POST("api/pointPay")
    @FormUrlEncoded
    Call<BaseResponse<Object>> pointPay(@Header("X-AUTH-TOKEN") String token, @Field("trade_id") String trade_id, @Field("verifySecret") String verifySecret);


    /**
     * 验证支付密码
     */
    @POST("api/verifySecret")
    @FormUrlEncoded
    Call<BaseResponse<Object>> verifyPayPassword(@Header("X-AUTH-TOKEN") String token, @Field("verifySecret") String verifySecret);


    /**
     * 积分提现
     */
    @POST("api/exchange")
    @FormUrlEncoded
    Call<BaseResponse<List<ExchangeInfo>>> canCashExchange(@Header("X-AUTH-TOKEN") String token, @Field("verifySecret") String verifySecret, @Field("money") String money);


    /**
     * 货款提现
     */
    @POST("api/exchangeSurplus")
    @FormUrlEncoded
    Call<BaseResponse<List<ExchangeInfo>>> exchangeSurplus(@Header("X-AUTH-TOKEN") String token, @Field("verifySecret") String verifySecret, @Field("money") String money);


    /**
     * 获取好友列表
     */
    @POST("api/myFriend")
    @FormUrlEncoded
    Call<BaseResponse<List<RecomFriendBase>>> getFriends(@Header("X-AUTH-TOKEN") String token, @Field("type") int type);

    /**
     * 推荐面板信息
     *
     * @param token
     * @return
     */
    @POST("api/myReferInformation")
    Call<BaseResponse<List<RecommendInfo>>> myReferInformation(@Header("X-AUTH-TOKEN") String token);


    /**
     * 获取普通商品订单列表
     */
    @POST("api/myOrderList")
    @FormUrlEncoded
    Call<BaseResponse<List<OrderMailInfo>>> getMainOrderList(@Header("X-AUTH-TOKEN") String token, @Field("status") int status);


    /**
     * 获取智品订单购买列表
     */
    @POST("api/myBuyOrderLog")
    Call<BaseResponse<List<OrderInfo>>> myBuyOrderLog(@Header("X-AUTH-TOKEN") String token);

    /**
     * 获取智品订单提货列表
     */
    @POST("api/myDeliveryOrders")
    Call<BaseResponse<List<OrderInfo>>> myDeliveryOrders(@Header("X-AUTH-TOKEN") String token);


    /**
     * 确认收货
     *
     * @param token
     * @param order_sn
     * @return
     */
    @POST("api/confirmReceipt")
    @FormUrlEncoded
    Call<BaseResponse<Object>> confirmReceipt(@Header("X-AUTH-TOKEN") String token, @Field("order_sn") String order_sn);


    /**
     * 查询微信支付结果
     *
     * @param token
     * @param trade_id
     * @return
     */
    @POST("api/queryWXOrder")
    @FormUrlEncoded
    Call<BaseResponse<List<Object>>> queryWXOrder(@Header("X-AUTH-TOKEN") String token, @Field("trade_id") String trade_id);


    /**
     * 获取忘记支付密码token
     */
    @POST("api/getResetPayPwdToken")
    @FormUrlEncoded
    Call<BaseResponse<String>> getResetPayPwdToken(@Header("X-AUTH-TOKEN") String token, @Field("mobile") String mobile, @Field("code") String code);


    /**
     * 修改密码
     */
    @POST("supplier/getBinkBankToken")
    @FormUrlEncoded
    Call<BaseResponse<Object>> getBinkBankToken(@Header("X-AUTH-TOKEN") String token, @Field("code") String code);

    /**
     * 获取支付验证码
     */
    @POST("supplier/getVerifyCode")
    @FormUrlEncoded
    Call<BaseResponse<String>> getVerifyCode(@Header("X-AUTH-TOKEN") String token, @Field("type") int type);

    /**
     * 充值支付密码
     */
    @POST("api/resetPayPwdPost")
    @FormUrlEncoded
    Call<BaseResponse<String>> resetPayPwdPos(@Header("X-AUTH-TOKEN") String token, @Field("mobile") String mobile, @Field("reset_token") String reset_token, @Field("password") String password);


    /**
     * 获取消息列表
     */
    @POST("supplier/myMessage")
    @FormUrlEncoded
    Call<BaseResponse<MessageBase>> myMessage(@Header("X-AUTH-TOKEN") String token,
                                              @Field("msg_type") String msg_type,
                                              @Field("page") String page);


    /**
     * 获取消息列表
     */
    @POST("api/mynotify")
    Call<BaseResponse<List<Message>>> myMessage(@Header("X-AUTH-TOKEN") String token);


    /**
     * 激活好友
     */
    @POST("api/activeFriend")
    @FormUrlEncoded
    Call<BaseResponse<String>> activateFriend(@Header("X-AUTH-TOKEN") String token, @Field("mobile") String mobile);

    /**
     * 获取激活记录
     */
    @POST("api/activeFriendLog")
    Call<BaseResponse<List<AmountDetail>>> activeFriendLog(@Header("X-AUTH-TOKEN") String token);

    /**
     * 获取智品积分列表
     */
    @POST("api/intelligencePointsLog")
    Call<BaseResponse<List<IntegralPoint>>> intelligencePointsLog(@Header("X-AUTH-TOKEN") String token);


    /**
     * 获取平台库存信息
     */
    @POST("api/getstocks")
    Call<BaseResponse<List<Inventory>>> getstocks(@Header("X-AUTH-TOKEN") String token);

    /**
     * 获取平台库存明细列表
     */
    @POST("api/getstocksLog")
    Call<BaseResponse<List<InventoryDetails>>> getstocksLog(@Header("X-AUTH-TOKEN") String token);

    /**
     * 提货
     *
     * @param token
     * @param addr_id
     * @param good_id
     * @param number
     * @return
     */
    @POST("api/pickUpGood")
    @FormUrlEncoded
    Call<BaseResponse<List<OrderPay>>> pickUpGood(@Header("X-AUTH-TOKEN") String token, @Field("addr_id") String addr_id, @Field("good_id") String good_id, @Field("number") String number, @Field("verifySecret") String verifySecret);


    /**
     * 获取物流信息
     *
     * @param order_sn
     * @return
     */
    @POST("common/getExpressInfomation")
    @FormUrlEncoded
    Call<Object> getExpressInfomation(@Field("order_sn") String order_sn);

    /**
     * 订单详情
     *
     * @param token
     * @param order_id
     * @return
     */
    @POST("api/myOrderListDetail")
    @FormUrlEncoded
    Call<BaseResponse<List<OrderDetals>>> myOrderListDetail(@Header("X-AUTH-TOKEN") String token, @Field("order_id") String order_id);

    /**
     * 智品订单详情
     *
     * @param token
     * @param order_sn
     * @return
     */
    @POST("api/myOrdersDetail")
    @FormUrlEncoded
    Call<BaseResponse<List<WisdomOrderDetals>>> myOrdersDetail(@Header("X-AUTH-TOKEN") String token, @Field("order_sn") String order_sn);


    /**
     * 查询运费
     *
     * @param token
     * @param addr_id
     * @param good_id
     * @return
     */
    @POST("api/getDeliveryPriceByAddId")
    @FormUrlEncoded
    Call<BaseResponse<List<Shipping>>> getDeliveryPriceByAddId(@Header("X-AUTH-TOKEN") String token, @Field("addr_id") String addr_id, @Field("good_id") String good_id);


    /**
     * 删除购物车商品
     *
     * @param token
     * @return
     */
    @POST("api/deleteShoppingCartList")
    @FormUrlEncoded
    Call<BaseResponse<Object>> delCartGoodsBat(@Header("X-AUTH-TOKEN") String token, @Field("shopcartId") String shopcartId);


    /**
     * 获取购物车列表
     */
    @POST("api/shoppingCartList")
    Call<BaseResponse<List<CartGoodsBase>>> queryUserCart(@Header("X-AUTH-TOKEN") String token);

    /**
     * 加入购物车
     *
     * @param token
     * @param goods_attr_id
     * @param number
     * @return
     */
    @POST("api/addShoppingCart")
    @FormUrlEncoded
    Call<BaseResponse<Object>> addShoppingCart(@Header("X-AUTH-TOKEN") String token, @Field("goods_attr_id") String goods_attr_id, @Field("number") String number);


    /**
     * 取类目及一级标签
     *
     * @return
     */
    @POST("common/getCategary")
    @Headers("Cache-Control: public, max-age=120")
    Call<BaseResponse<List<ParentCatergory>>> getCategoryChildlistRepo();


    /**
     * 根据一级分类取二级分类
     *
     * @return
     */
    @POST("common/goodCategary")
    @Headers("Cache-Control: public, max-age=120")
    @FormUrlEncoded
    Call<BaseResponse<List<Catergory>>> goodCategary(@Field("jump_id") String jump_id);


    /**
     * 根据一级分类取二级分类
     *
     * @return
     */
    @POST("common/goodCategaryList")
    @Headers("Cache-Control: public, max-age=120")
    @FormUrlEncoded
    Call<BaseResponse<List<GoodsBean>>> goodCategaryList(@Field("category_id") String category_id);

    /**
     * 搜索商品
     *
     * @param name
     * @return
     */
    @POST("common/searchGood")
    @FormUrlEncoded
    Call<BaseResponse<List<GoodsBean>>> searchGood(@Field("name") String name);


    /*
    *修改昵称
     */
    @POST("api/updateName")
    @FormUrlEncoded
    Call<BaseResponse<Object>> updateNick(@Header("X-AUTH-TOKEN") String token, @Field("nickname") String name);

    /**
     * 修改头像
     *
     * @param token
     * @param headpic
     * @return
     */
    @POST("api/updateHeadPic")
    @FormUrlEncoded
    Call<BaseResponse<Object>> updateHeadPic(@Header("X-AUTH-TOKEN") String token, @Field("headpic") String headpic);

    /*
    *评价
     */
    @POST("api/submitComment")
    @FormUrlEncoded
    Call<BaseResponse<Object>> submitComment(@Header("X-AUTH-TOKEN") String token, @Field("good_id") String good_id, @Field("attr_id") String attr_id, @Field("order_sn") String order_sn, @Field("comment") String comment, @Field("level") int level);

    /**
     * 评论列表
     * @param token
     * @param good_id
     * @param status 0  全部  1 好评  2  中评  3  差评
     * @return
     */
    @POST("api/commentList")
    @FormUrlEncoded
    Call<BaseResponse<List<CommentContent>>> commentList(@Header("X-AUTH-TOKEN") String token, @Field("good_id") String good_id, @Field("status") int status);


    /**
     * 申请售后
     * @param token
     * @return
     */
    @POST("api/submitExchange")
    @FormUrlEncoded
    Call<BaseResponse<Object>> submitExchange(@Header("X-AUTH-TOKEN") String token,
                                              @Field("reason") String reason,
                                              @Field("attr_id") String attr_id,
                                              @Field("order_sn") String order_sn,
                                              @Field("pic") String pic
                                             );
    /**
     * 售后订单列表
     * @param token
     * @return
     */
    @POST("api/exchangeGoodList")
    Call<BaseResponse<List<AfterSaleGoodsInfo>>> exchangeGoodList(@Header("X-AUTH-TOKEN") String token);



}
