package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.ItemDOMapper;
import com.miaoshaproject.dao.ItemStockDOMapper;
import com.miaoshaproject.dao.SequenceDOMapper;
import com.miaoshaproject.dataobject.ItemDO;
import com.miaoshaproject.dataobject.ItemStockDO;
import com.miaoshaproject.error.BussinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.PromoModel;
import com.miaoshaproject.validator.ValidationResult;
import com.miaoshaproject.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author aixscode.github.io
 * @date 2019/1/8 15:29
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private SequenceDOMapper sequenceDOMapper;

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private ItemDOMapper itemDOMapper;

    @Autowired
    private PromoService promoService;
    @Autowired
    private ItemStockDOMapper itemStockDOMapper;

    private ItemDO convertItemDOFromItemModel(ItemModel itemModel)
    {
        if(itemModel==null)
        {
            return  null;
        }
        ItemDO itemDo = new ItemDO();
        BeanUtils.copyProperties(itemModel,itemDo);
        itemDo.setPrice(itemModel.getPrice().doubleValue());
        return  itemDo;
    }

    private ItemStockDO convertItemStockDOFromItemModel(ItemModel itemModel)
    {
        if(itemModel== null) {

        return  null;
        }
        ItemStockDO itemStockDO = new ItemStockDO();
        itemStockDO.setItemId(itemModel.getId());
        itemStockDO.setStock(itemModel.getStock());
        return  itemStockDO;
    }

    @Override
    @Transactional
    public ItemModel createItem(ItemModel itemModel)  {
        /*
        //校验入参
        ValidationResult result =validator.validate(itemModel);

        if(result.isHasError())
        {
                throw  new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,result.getErrMsg());
        }
        */
        //转化itemmodel->dataobject

        ItemDO itemDO = this.convertItemDOFromItemModel(itemModel);
        //写入数据库
        itemDOMapper.insertSelective(itemDO);

        itemModel.setId(itemDO.getId());

        ItemStockDO itemStockDO=this.convertItemStockDOFromItemModel(itemModel);

        itemStockDOMapper.insertSelective(itemStockDO);
        //返回创建完成对象

        return this.getItemById(itemModel.getId());
    }

    @Override
    public List<ItemModel> listItem()
    {
        List<ItemDO> itemDOList = itemDOMapper.listItem();
        List<ItemModel> itemModelList=itemDOList.stream().map(itemDO -> {
            ItemStockDO itemStockDO =itemStockDOMapper.selectByItemId(itemDO.getId());
            ItemModel itemModel= this.convertModelFromDataObject(itemDO,itemStockDO);
            return itemModel;
        }).collect(Collectors.toList());
        return itemModelList;
    }

    @Override
    public ItemModel getItemById(Integer id) {

        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(id);

        if(itemDO == null)
        {
            return  null;
        }

        //操作获得库存数量

        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
        //将dataobject转化为model
        ItemModel itemModel =convertModelFromDataObject(itemDO,itemStockDO);

        //获取活动商品信息

        PromoModel promoModel = promoService.getPromoByItemId(itemModel.getId());

        if (promoModel != null && promoModel.getStatus().intValue()!=3)
        {
            itemModel.setPromoModel(promoModel);
        }
        return  itemModel;
    }

    @Override
    @Transactional
    public boolean decreaseStock(Integer itemId, Integer amount) throws BussinessException {

        int affectRow = itemStockDOMapper.decreaseStock(itemId,amount);
        if(affectRow>0)
        {
            //更新数据库
            return  true;
        }
        else
        {
            return false;
        }
    }

    @Override
    @Transactional
    public void increaseSales(Integer itemId, Integer amount) throws BussinessException {
        itemDOMapper.increaseSales(itemId,amount);

    }

    private  ItemModel convertModelFromDataObject(ItemDO itemDO ,ItemStockDO itemStockDO)
    {

        ItemModel itemModel = new ItemModel();

        BeanUtils.copyProperties(itemDO,itemModel);

        itemModel.setPrice(new BigDecimal(itemDO.getPrice()));

        itemModel.setStock(itemStockDO.getStock());

        return  itemModel;
    }
}
