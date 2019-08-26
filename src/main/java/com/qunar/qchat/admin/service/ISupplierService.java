package com.qunar.qchat.admin.service;

import com.qunar.qchat.admin.model.BusiSupplierMapping;
import com.qunar.qchat.admin.model.Supplier;
import com.qunar.qchat.admin.model.SupplierInfo;
import com.qunar.qchat.admin.vo.BusiReturnResult;
import com.qunar.qchat.admin.vo.DeptUserVO;
import com.qunar.qchat.admin.vo.SupplierGroupVO;
import com.qunar.qchat.admin.vo.SupplierVO;
import com.qunar.qchat.admin.vo.third.SupplierOperatorInfo;

import java.util.List;
import java.util.Map;

/**
 * Created by hongwu.yang on 2015年10月14日.
 */
public interface ISupplierService {

    public List<Supplier> getSupplierList();

    /**
     * 添加供应商
     * @param supplierVO
     * @return
     */
    public BusiReturnResult saveSupplier(SupplierVO supplierVO);

    /**
     * 添加供应商
     * @param supplier
     * @return
     */
    public BusiReturnResult saveSupplierEx(SupplierVO supplier);

    /**
     * 编辑供应商信息
     * @param supplierVO
     * @return
     */
    public BusiReturnResult updateSupplier(SupplierVO supplierVO);

    /**
     * 编辑供应商信息,全量修改
     * @param supplier
     * @return
     */
    public BusiReturnResult updateFullSupplier(Supplier supplier);

    /**
     * 根据用户中心登陆名获取供应商信息
     * @param qunarName 管理员
     * @return
     */
    List<SupplierVO> getSupplierByQunarName(String qunarName);

    List<SupplierVO> getSupplierByQunarName(String qunarName, int pageNum, int pageSize, int businessId);

    Long getPageCountSupplier(String qunarName, int businessId);
    /**
     * 获取客服所属的供应商
     * @param qName
     * @param bType
     * @return
     */
    List<SupplierVO> getSupplierBySeatQName(String qName, int bType);
    
    List<SupplierVO> getSupplierBySeatQName(String qName);

    /**
     * 根据业务线供应商编号+业务类型获取供应商信息
     * @param busiSupplierId
     * @param busiType
     * @return
     */
    SupplierVO getSupplierByBusiSupplierId(String busiSupplierId,int busiType);

    /**
     * 根据业务线供应商编号+业务类型获取供应商信息
     * @param busiSupplierId
     * @param busiType
     * @return
     */
    Supplier getSupplierByBusiSupplierIdEx(String busiSupplierId,int busiType);
    /**
     *
     * @param supplierId
     * @param qunarNameList
     * @return 返回执行插入成功的映射关系数
     */
    int updateSupplierQunarNameMapping(long supplierId,List<String> qunarNameList);


    int updateSupplierSysUsers(long supplierId, List<String> addList, List<String> delList);

    List<SupplierGroupVO> getSuGroupList(List<Long> suIdList);

    /**
     * 根据客服qunarName查找其所在供应商对应的运营
     */
    List<DeptUserVO> getOperatorsBySeatQunarName(String seatQunarName);

    /**
     * 保存供应商的运营人员
     */
    boolean saveSupplierOperator(SupplierOperatorInfo supplierOperatorInfo);

    /**
     * 获取供应商信息
     */
    List<Supplier> getSupplierByIds(List<Long> ids);

    /**
     * 保存供应商信息
     */
    void saveSupplierInfo(List<SupplierInfo> supplierInfos);

    /**
     * 根据qchat供应商id查询供应商信息
     */
    BusiSupplierMapping getSupplierBySupplierId(long supplierId);

    /**
     * 店铺维度配置欢迎语
     */
    boolean updateWelcomes(List<Supplier> suppliers);

    /**
     * 查询供应商欢迎语
     */
    List<Map> querySupplierWelcomes(List<Long> supplierIds);

    /**
     * 查询某客服对应的欢迎语
     */
    String queryWelcomesBySeat(String seatQName, Long seatId);

    /**
     * 查询店铺的欢迎语
     */
    String queryWelcomesById(Long supplierId);

    /**
     * 从管理员有权限的商铺里查找该店铺的名称
     * @param busiSupplierName
     * @param curBuSuList
     * @return
     */
    List<SupplierVO> filterSupplierByBusiSupplierName(String busiSupplierName, List<SupplierVO> curBuSuList);
}
