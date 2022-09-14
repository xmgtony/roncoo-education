package com.roncoo.education.course.service.auth.biz;

import cn.hutool.core.collection.CollUtil;
import com.roncoo.education.common.config.ThreadContext;
import com.roncoo.education.common.core.base.Page;
import com.roncoo.education.common.core.base.PageUtil;
import com.roncoo.education.common.core.base.Result;
import com.roncoo.education.common.core.tools.BeanUtil;
import com.roncoo.education.common.service.BaseBiz;
import com.roncoo.education.course.dao.CourseDao;
import com.roncoo.education.course.dao.UserCourseDao;
import com.roncoo.education.course.dao.impl.mapper.entity.Course;
import com.roncoo.education.course.dao.impl.mapper.entity.UserCourse;
import com.roncoo.education.course.dao.impl.mapper.entity.UserCourseExample;
import com.roncoo.education.course.service.auth.req.AuthUserCourseReq;
import com.roncoo.education.course.service.auth.resp.AuthCourseSignResp;
import com.roncoo.education.course.service.auth.resp.AuthUserCourseResp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AUTH-课程用户关联表
 *
 * @author wujing
 */
@Component
@RequiredArgsConstructor
public class AuthUserCourseBiz extends BaseBiz {

    @NotNull
    private final UserCourseDao dao;
    @NotNull
    private final CourseDao courseDao;

    public Result<Page<AuthUserCourseResp>> listForPage(AuthUserCourseReq req) {
        UserCourseExample example = new UserCourseExample();
        example.createCriteria().andUserIdEqualTo(ThreadContext.userId());
        Page<UserCourse> userCoursePage = dao.page(req.getPageCurrent(), req.getPageSize(), example);
        Page<AuthUserCourseResp> respPage = PageUtil.transform(userCoursePage, AuthUserCourseResp.class);
        if (CollUtil.isNotEmpty(respPage.getList())) {
            List<Long> courseIdList = respPage.getList().stream().map(AuthUserCourseResp::getCourseId).collect(Collectors.toList());
            List<Course> courseList = courseDao.listByIds(courseIdList);
            Map<Long, Course> courseMap = courseList.stream().collect(Collectors.toMap(item -> item.getId(), item -> item));
            for (AuthUserCourseResp resp : respPage.getList()) {
                resp.setCourseResp(BeanUtil.copyProperties(courseMap.get(resp.getCourseId()), AuthCourseSignResp.class));
            }
        }
        return Result.success(respPage);
    }

}
