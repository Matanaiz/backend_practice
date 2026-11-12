package org.csu.mypetstore.service;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.csu.mypetstore.dto.UserReadResult;
import org.csu.mypetstore.entity.User;
import org.csu.mypetstore.mapper.UserMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UserService 的纯单元测试。
 *
 * <p>使用 Mockito 模拟 UserMapper，不启动 Spring 容器、不连接数据库。
 * 由于 MyBatis-Plus 的 LambdaQueryWrapper 需要实体元数据缓存，
 * 在测试类中做必要的最小初始化（仅初始化 User 的 TableInfo 并安装 lambda 缓存），
 * 不为此启动完整应用。</p>
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    /**
     * 最小化初始化 MyBatis-Plus 实体元数据，使 LambdaQueryWrapper 能正常解析 lambda 引用。
     * 仅初始化 User 实体，不启动 Spring 容器、不连接数据库。
     */
    @BeforeAll
    static void initMybatisPlusMetadata() {
        Configuration configuration = new Configuration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        assistant.setCurrentNamespace("org.csu.mypetstore.mapper.UserMapper");
        TableInfo tableInfo = TableInfoHelper.initTableInfo(assistant, User.class);
        LambdaUtils.installCache(tableInfo);
    }

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setUsername("alice");
        sampleUser.setDisplayName("Alice Wang");
        sampleUser.setRole("USER");
        sampleUser.setStatus("ACTIVE");
    }

    // ---------- 场景 1：查询到用户时正确转换为 UserReadResult ----------

    @Test
    @DisplayName("查询到用户时，应返回包含正确字段的 UserReadResult")
    void findByUsername_found_shouldReturnUserReadResult() {
        // given
        when(userMapper.selectOne(any())).thenReturn(sampleUser);

        // when
        Optional<UserReadResult> result = userService.findByUsername("alice");

        // then
        assertThat(result).isPresent();
        UserReadResult dto = result.get();
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.username()).isEqualTo("alice");
        assertThat(dto.displayName()).isEqualTo("Alice Wang");
        assertThat(dto.role()).isEqualTo("USER");
        assertThat(dto.status()).isEqualTo("ACTIVE");
        verify(userMapper, times(1)).selectOne(any());
    }

    // ---------- 场景 2：查询不到用户时返回空 Optional ----------

    @Test
    @DisplayName("查询不到用户时，应返回空 Optional")
    void findByUsername_notFound_shouldReturnEmptyOptional() {
        // given
        when(userMapper.selectOne(any())).thenReturn(null);

        // when
        Optional<UserReadResult> result = userService.findByUsername("nobody");

        // then
        assertThat(result).isEmpty();
        verify(userMapper, times(1)).selectOne(any());
    }

    // ---------- 场景 3：Mapper 抛出 DataAccessResourceFailureException 时继续抛出同一异常 ----------

    @Test
    @DisplayName("Mapper 抛出 DataAccessResourceFailureException 时，Service 应原样继续抛出")
    void findByUsername_mapperThrows_shouldPropagateSameException() {
        // given
        DataAccessResourceFailureException expectedException =
                new DataAccessResourceFailureException("数据库连接失败");
        when(userMapper.selectOne(any())).thenThrow(expectedException);

        // when & then
        assertThatThrownBy(() -> userService.findByUsername("alice"))
                .isSameAs(expectedException);
        verify(userMapper, times(1)).selectOne(any());
    }
}
