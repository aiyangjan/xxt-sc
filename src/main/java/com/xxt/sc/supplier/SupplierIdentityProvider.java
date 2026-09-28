package com.xxt.sc.supplier;

/**
 * 供应商身份来源。
 *
 * <p>PRD 4.6 / AC-102：supplierId 必须从登录身份和绑定关系推导，
 * 任何 Controller 都不得直接接收 supplierId 作为数据范围依据。
 * 实现类需要接入统一认证体系后提供真实实现。
 */
public interface SupplierIdentityProvider {

    /**
     * 当前登录身份绑定的供应商 ID。
     *
     * @throws com.xxt.sc.common.exception.BizException 未登录或未绑定供应商时抛出 UNAUTHORIZED / FORBIDDEN
     */
    long currentSupplierId();
}
