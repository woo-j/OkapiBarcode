/*
 * Copyright 2015 Robin Stuart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.org.okapibarcode;

/**
 * Barcode Type.
 *
 * @author anyonetff
 */
public enum SymbolType {

    code11(1, 1),
    c25matrix(2, 2),
    c25inter(3, 1),
    c25iata(4, 1),
    c25logic(6, 1),
    c25ind(7, 1),
    code39(8, 1),
    excode39(9, 1),
    /**
     * EAN (Including EAN-8 and EAN-13).
     */
    eanx(13, 1),
    /**
     * EAN13 + check digit.
     */
    eanx_chk(14, 1),
    /**
     * EAN128/GS1-128.
     * <p>
     * EAN-128，我国所推行的128码，根据EAN/UCC-128码定义标准将资料转变成条码符号，并采用128码逻辑，具有完整性、紧密性、连结性及高可靠度的特性。辨识范围涵盖生产过程中一些补充性质且易变动之资讯，如生产日期、批号、计量等。可应用于货运栈版标签、携带式资料库、连续性资料段、流通配送标签等。</p>
     * <p>
     * GS1 (Globe standard
     * 1)是1973年由美国统一代码委员会建立的组织，该系统拥有全球跨行业的产品、运输单元、资产、位置和服务的标识标准体系和信息交换标准体系，使产品在全世界都能够被扫描和识读
     * 。</p>
     */
    ean128(16, 1),
    codabar(18, 1),
    /**
     * Code 128.
     * <p>
     * CODE128码是广泛应用在企业内部管理、生产流程、物流控制系统方面的条码码制，由于其优良的特性在管理信息系统的设计中被广泛使用，CODE128码是应用最广泛的条码码制之一。</p>
     * <p>
     * CODE128码是1981年引入的一种高密度条码，CODE128 码可表示从 ASCII 0 到ASCII 127
     * 共128个字符，故称128码。其中包含了数字、字母和符号字符。</p>
     */
    code128(20, 1),
    dpleit(21, 1),
    dpident(22, 1),
    code16k(23, 1),
    code49(24, 1),
    code93(25, 1),
    flat(28, 1),
    rss14(29, 1),
    rss_ltd(30, 1),
    /**
     * GS1 DataBar Expanded Omnidirectional. 扩展型GS1条码
     */
    rss_exp(31, 2),
    telepen(32, 1),
    upca(34, 1),
    upca_chk(35, 1),
    upce(37, 1),
    upce_chk(38, 1),
    postnet(40, 1),
    msi_plessey(47, 1),
    fim(49, 1),
    logmars(50, 1),
    pharma(51, 1),
    pzn(521, 1),
    pharma_two(53, 1),
    /**
     * PDF417.
     * <p>
     * PDF417条码是一种高密度、高信息含量的便携式数据文件，是实现证件及卡片等大容量、高可靠性信息自动存储、携带并可用机器自动识读的理想手段。</p>
     */
    pdf417(55, 2),
    pdf417trunc(56, 2),
    maxicode(57, 2),
    /**
     * QR Code.
     * <p>
     * QR
     * Code码，是由Denso公司于1994年9月研制的一种矩阵二维码符号，它具有一维条码及其它二维条码所具有的信息容量大、可靠性高、可表示汉字及图象多种文字信息、保密防伪性强等优点。</p>
     */
    qrcode(58, 2),
    code128b(60, 1),
    auspost(63, 1),
    ausreply(66, 1),
    ausroute(67, 1),
    ausredirect(68, 1),
    /**
     * ISBN (EAN-13 with verification stage).
     * <p>
     * 全球书号及商品编码，商品包装上印刷的就是这个码。</p>
     */
    isbnx(69, 1),
    rm4scc(70, 1),
    /**
     * Data Matrix.
     * <p>
     * Datamatrix是二维码的一个成员，与1989年由美国国际资料公司发明，广泛用于商品的防伪、统筹标识。</p>
     */
    datamatrix(71, 2),
    /**
     * EAN14.
     * <p>
     * 这个条码就是GS1的01段，报文为13位数值，条码生成时自动补1位。输出样式与EAN128仅含01段时一致</p>
     */
    ean14(72, 1),
    vin(73, 1),
    codablockf(74, 1),
    nve18(75, 1),
    japanpost(76, 1),
    koreapost(77, 1),
    /**
     * GS1 DataBar Stacked.
     * <p>
     * DataBar（前称RSS）是一种条码符号，可用于识别小件物品，比起目前的EAN/UPC条形码可携带更多信息。</p>
     * <p>
     * Stacked代表堆叠模式，可以压缩打印尺寸</p>
     */
    rss14stack(79, 2),
    /**
     * GS1 DataBar Stacked Omnidirection.
     * <p>
     * Omnidirection代表全向扫描模式</p>
     */
    rss14stack_omni(80, 2),
    /**
     * GS1 DataBar Expanded Stacked Omnidirectional.
     * <p>
     * 全向识别模式</p>
     */
    rss_expstack(81, 2),
    planet(82, 1),
    micropdf417(84, 1),
    onecode(85, 2),
    plessey(86, 1),
    telepen_num(87, 1),
    itf14(89, 1),
    kix(90, 1),
    /**
     * Aztec Code.
     * <p>
     * Aztec Code是1995年，由Hand HeldProducts公司的Dr. Andrew
     * Longacre设计。它是一种高容量的二维条形码格式。它可以对ASCII和扩展ASCII码进行编码。当使用最高容量和25%的纠错级别的時候，Aztec可以对3000个字符或者3750个数字进行编码。</p>
     */
    aztec(92, 2),
    daft(93, 1),
    /**
     * Micro QR Code.
     * <p>
     * QR Code的一种，正方形，只有标准模式的四分之一尺寸。</p>
     */
    microqr(97, 2),
    hibc_128(98, 1),
    hibc_39(99, 1),
    hibc_dm(102, 2),
    hibc_qr(104, 2),
    hibc_pdf(106, 2),
    hibc_micpdf(108, 2),
    hibc_blockf(110, 2),
    hibc_aztec(112, 2),
    /**
     * DotCode.
     * <p>
     * DotCode是一种大小可变、形状多样、识别效率极高的矩阵条码符号，设计的初衷为快速标记应用，在生产线不间断快速读取时尤见成效。</p>
     */
    dotcode(115, 2),
    /**
     * Han Xin (Chinese Sensible) Code.
     * <p>
     * 汉信码由中国物品编码中心完成的国家“十五”重大科技专项——《二维条码新码制开发与关键技术标准研究》取得了突破性成果，研究成果包括：研究开发汉信码新码制、开发汉信码生成软件、开发汉信码识读技术及算法、汉信码硬件设备研发、汉信码装备研制、汉信码通讯技术研发以及编制汉信码国家标准等的系列工作。</p>
     */
    hanxin(116, 2),
    mailmark(121, 1),
    azrune(128, 1),
    code32(129, 1),
    eanx_cc(130, 1),
    ean128_cc(131, 1),
    rss14_cc(132, 1),
    rss_ltd_cc(133, 1),
    rss_exp_cc(134, 1),
    upca_cc(135, 1),
    upce_cc(136, 1),
    rss14stack_cc(137, 1),
    rss14_omni_cc(138, 1),
    rss_expstack_cc(139, 1),
    channel(140, 1),
    /**
     * Code One.
     * <p>
     * Code One是一种用成像设备识别的矩阵式二维条码。Code One符号中包含可由快速性线性探测器识别的识别图案。每一模块的宽和高的尺寸为X。
     * </p>
     */
    codeone(141, 2),
    gridmatrix(142, 2),
    upnqr(143, 2),
    ultra(144, 1),
    /**
     * Rectangular Micro QR Code.
     * <p>
     * QR Code的一种，长方形，只有标准模式的一半尺寸。</p>
     */
    rmqr(145, 2);

    /**
     * Zint Type Value.
     */
    private final int value;

    /**
     * 1D or 2D.
     */
    private final int dimension;

    private SymbolType(int value, int dimension) {
        this.value = value;
        this.dimension = dimension;
    }

    /**
     * @return the value
     */
    public int getValue() {
        return value;
    }

    public int getDimension() {
        return dimension;
    }
}
