package org.datn.service.impl;


import io.swagger.v3.oas.models.responses.ApiResponse;
import org.datn.DAO.AdminDao;
import org.datn.DAO.BlockUserDao;
import org.datn.DAO.UserDao;
import org.datn.bean.ResponseData;
import org.datn.entity.Admin;
import org.datn.entity.BlockUser;
import org.datn.entity.User;
import org.datn.service.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
public class UserAccountServiceImpl implements UserAccountService {
    @Autowired
    UserDao udao;
    @Autowired
    AdminDao adao;
    @Autowired
    BlockUserDao bldao;
    int x = 0;
    @Override
    public User findById(String email) {
        return udao.findByEmail(email);
    }

    @Override
    public BlockUser findByEmailInBlockUser(User u) {
        return udao.findByEmailInBlockUser(u);
    }

    @Override
    public BlockUser findByIpInBlockUser(String i,Instant d) {
        return udao.findByIPInBlockUser(i,d);
    }

    @Override
    public BlockUser createIpInBlockUser(BlockUser bl) {
        return bldao.save(bl);
    }

    @Override
    public Instant findEffectUntilInBlockUser(String ip) {
        return udao.findEffectUntilInBlockUser(ip);
    }

    @Override
    public List<User> findAll() {
        return udao.findAll();
    }

    @Override
    public Admin findByIdAdmin(String username) {
        return adao.findByUsernameAdmin(username).getAdmin();
    }

    @Override
    public List<Admin> findAllAdmin() {
        return adao.findAll();
    }

    @Override
    public List<User> getAdministrators() {
        return udao.getAdministrators();
    }

    @Override
    public ResponseData login(User user) throws UnknownHostException {

        ResponseData responseData=new ResponseData();
        String ip = InetAddress.getLocalHost().getHostAddress();    //l???y ip

        User user1 = udao.findOneByEmailIgnoreCaseAndPassword(user.getEmail(),user.getPassword());  //sai email ho???c m???t kh???u
        User user2=udao.findByEmail(user.getEmail());       //email kh??ng t???n t???i
        Instant time_until=udao.findEffectUntilInBlockUser(ip);                 //l???y th???i gian cu???i
        BlockUser user4 = udao.findByIPInBlockUser(ip , time_until);             //t??m user block theo ip

        if (user4 != null  ){
            System.out.println(user4.getIpAddress());
            Instant time_now = Instant.now();                           //l???y time hi???n t???i
            System.out.println(time_now.isAfter(time_until));
            if (time_now.isAfter(time_until)==false){
                responseData.setStatus(HttpStatus.LOCKED.value());
                responseData.setMessage("B??? kh??a trong 15'");
            }}else {
                if (user2 == null){
                    responseData.setStatus(HttpStatus.NOT_FOUND.value());
                    responseData.setMessage("Email kh??ng t???n t???i");
                } else if (user1 == null) {
                    responseData.setStatus(HttpStatus.NO_CONTENT.value());
                    responseData.setMessage("Email ho???c m???t kh???u sai");
                }else {
                    BlockUser user3 =udao.findByEmailInBlockUser(user2);
                    if (user3==null){
                        responseData.setStatus(HttpStatus.OK.value());
                        responseData.setMessage("????ng nh???p th??nh c??ng");
                    }else {
                        responseData.setStatus(HttpStatus.FORBIDDEN.value());
                        responseData.setMessage("T??i kho???n ???? b??? kh??a");
                    }
                }
            }


        responseData.setError(responseData.getMessage());
        return  responseData;
    }


}
//if (user2 == null){
//                responseData.setStatus(404);
//                responseData.setMessage("sai email ho???c m???t kh???u");
//            } else if (user1 == null){
//                responseData.setStatus(201);
//
//                responseData.setMessage("email kh??ng t???n t???i");
//            } else {
//                BlockUser user3=udao.findByEmailInBlockUser(user2);
//
//                if (user3 == null){
//                    responseData.setStatus(200);
//                }
//                else {
//                    responseData.setStatus(405);
//                    responseData.setMessage("T??i kho???n ???? b??? kh??a");
//                }
//
//            }