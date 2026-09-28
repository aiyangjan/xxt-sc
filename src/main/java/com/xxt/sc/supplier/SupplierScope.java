package com.xxt.sc.supplier;

import com.xxt.sc.common.exception.BizException;
import com.xxt.sc.common.result.ErrorCode;

/**
 * 供应链数据范围处理。
 *
 * <p>对应验收标准 AC-102：供应商 A 传入 supplierId=B 时，服务端必须
 * <strong>忽略伪造参数</strong>，并按登录身份绑定的范围返回数据。
 *
 * <p>这里区分两种语义，不要混用：
 * <ul>
 *   <li>{@link #resolve(Long, long)}：忽略伪造参数，直接使用登录身份推导值。
 *       适用于查询、详情、导出等「读」场景，符合 AC-102 的默认行为。</li>
 *   <li>{@link #assertInScope(long, long)}：发现越权直接拒绝。
 *       适用于结算、提现、配置变更等「高风险写」场景，避免静默降级导致资金风险。</li>
 * </ul>
 */
public final class SupplierScope {

    private SupplierScope() {
    }

    /**
     * 解析实际生效的 supplierId：忽略请求传入的值，始终以登录身份推导值为准。
     *
     * @param requestedSupplierId 请求参数中的 supplierId（可能为伪造值，可为 null）
     * @param currentSupplierId   由登录身份和绑定关系推导出的 supplierId
     * @return 实际用于数据过滤的 supplierId
     */
    public static long resolve(Long requestedSupplierId, long currentSupplierId) {
        // 请求参数一律不参与数据范围决策，仅保留日志/审计用途由调用方处理
        return currentSupplierId;
    }

    /**
     * 严格校验：请求参数与登录身份不一致时直接拒绝。
     *
     * @param requestedSupplierId 请求参数中的 supplierId
     * @param currentSupplierId   由登录身份推导出的 supplierId
     * @throws BizException SUPPLIER_SCOPE_DENIED 越权
     */
    public static void assertInScope(long requestedSupplierId, long currentSupplierId) {
        if (requestedSupplierId != currentSupplierId) {
            throw new BizException(ErrorCode.SUPPLIER_SCOPE_DENIED,
                    "请求的数据范围超出当前账号绑定的供应商");
        }
    }
}
