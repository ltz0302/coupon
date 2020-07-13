package com.ltz.coupon.service;

import com.ltz.coupon.dao.PathRepository;
import com.ltz.coupon.entity.Path;
import com.ltz.coupon.vo.CreatePathRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <h1>路径相关的服务功能实现</h1>
 */
@Slf4j
@Service
public class PathService {

    private final PathRepository pathRepository;

    @Autowired
    public PathService(PathRepository pathRepository) {
        this.pathRepository = pathRepository;
    }

    /**
     * <h2>添加新的 path 到数据表中</h2>
     *
     * @param request {@link CreatePathRequest}
     * @return Path 数据记录的主键
     */
    public List<Integer> createPath(CreatePathRequest request) {

        List<CreatePathRequest.PathInfo> pathInfos = request.getPathInfos();
        List<CreatePathRequest.PathInfo> validRequests = new ArrayList<>(request.getPathInfos().size());
        //调用方要保证request的pathinfo不为空
        //从数据库中根据服务名找到所有相关的path记录
        List<Path> currentPaths = pathRepository.findAllByServiceName(pathInfos.get(0).getServiceName());

        if (!CollectionUtils.isEmpty(currentPaths)) {
            for (CreatePathRequest.PathInfo pathInfo : pathInfos) {
                boolean isValid = true;
                /* 判断请求中的pathinfo是否已经在数据库中存在 */
                for (Path currentPath : currentPaths) {
                    if (currentPath.getPathPattern()
                            .equals(pathInfo.getPathPattern()) &&
                            currentPath.getHttpMethod().equals(pathInfo.getHttpMethod())) {
                        isValid = false;
                        break;
                    }
                }

                if (isValid) {
                    validRequests.add(pathInfo);
                }
            }
        }
        else {
            validRequests = pathInfos;
        }
        List<Path> paths = new ArrayList<>(validRequests.size());
        validRequests.forEach(p -> paths.add(new Path(
                p.getPathPattern(),
                p.getHttpMethod(),
                p.getPathName(),
                p.getServiceName(),
                p.getOpMode()
        )));

        return pathRepository.saveAll(paths)
                .stream().map(Path::getId).collect(Collectors.toList());
    }
}
