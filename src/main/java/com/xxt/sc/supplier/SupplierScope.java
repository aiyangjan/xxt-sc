package com.xxt.sc.supplier;

import com.xxt.sc.common.exception.BizException;
import com.xxt.sc.common.result.ErrorCode;

/**
 * 供应链数据范围校验。
 *
 * <p>对应验收标准 AC-102：供应商 A 传入 supplierId=B 时，服务端必须忽略伪造参数，
 * 并按登录绑定范围返回数据；越权直接拒绝，不静默降级。
 */
public final class SupplierScope {

    private SupplierScope() {
    }

    /**
     * 校验请求中的 supplierId 是否属于当前登录供应商。
     *
     * @param requestedSupplierId 请求参数中的 supplierId（可能为伪造值）
     * @param currentSupplierId   由登录身份推导出的 supplierId
     */
    public static void assertInScope(long requestedSupplierId, long currentSupplierId) {
        if (requestedSupplierId != currentSupplierId) {
            throw new BizException(ErrorCode.SUPPLIER_SCOPE_DENIED,
                    "请求的数据范围超出当前账号绑定的供应商");
        }
    }
}
