package com.monster.monsteraicode.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.monster.monsteraicode.entity.App;
import com.monster.monsteraicode.mapper.AppMapper;
import com.monster.monsteraicode.service.AppService;
import org.springframework.stereotype.Service;

/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com/monster999">monster</a>
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

}
