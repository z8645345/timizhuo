<template>
  <div>
    <section class="aui-content">
      <div class="aui-list-header">
        <div style="text-align: center; height: 2rem; font-size: 1rem; line-height: 2rem">
          <i class="aui-iconfont aui-icon-left aui-text-danger" style="float: left;font-size: 1rem; margin-left: 0.5rem" @click="goback"></i>
          <span v-if="type == 1">粉丝列表</span>
          <span v-else>关注人列表</span>
        </div>
      </div>
      <ul class="aui-list aui-media-list">
        <li class="aui-list-item" v-for="timiUser in followList">
          <div class="aui-card-list-header ">
            <router-link :to="{ name: 'orterUserHome', query: { userId: timiUser.id }}" style="width: 70%; float: left;">
              <div class="aui-card-list-user-avatar">
                <img :src="timiUser.pic" class="aui-img-round" style="vertical-align: middle">
              </div>
              <div style="height: 40px; line-height: 40px; vertical-align: middle;
-webkit-box-orient: vertical;
                                                    display: -webkit-box;
                                                    -webkit-line-clamp: 1;
                                                    text-overflow: -o-ellipsis-lastline;
                                                    overflow: hidden;
                                                    text-overflow: ellipsis;
                                                    "> {{timiUser.nickname}}
              </div>
            </router-link>

            <div v-if="type==1 && timiUser.isFollow" class="aui-font-size-14 text-light" style="width: 20%; float: right;">
              互相关注
            </div>
            <div v-else-if="type==1 && !timiUser.isFollow" @click="follow(timiUser.id)" class="aui-font-size-14 text-light aui-btn aui-btn-info" style="width: 20%; float: right;">
              +关注
            </div>
            <div v-else-if="type==2 && timiUser.isFollowMe" class="aui-font-size-14 text-light" style="width: 20%; float: right;">
              互相关注
            </div>
          </div>
        </li>
      </ul>
    </section>
  </div>
</template>

<script>
    export default {
        name: "user-follow-list",
      data() {
        return {
          followList: [],
          type: 1,
        }
      },
      mounted: function() {
        this.loadFollowList();
      },
      methods: {
        goback: function () {
          this.$router.go(-1);
        },
        loadFollowList: function () {
          var app = this;
          this.type = this.$route.query.type;
          var data = {
          }
          if ( this.type == 1) {
            data.parentId = this.$route.query.userId;
          } else if ( this.type == 2) {
            data.userId = this.$route.query.userId;
          }
          this.post('/timizhuo/fans/followList', data, function (res) {
            if (res.data.code == '200') {
              app.followList = res.data.data;
            } else {

            }
          }, function (err) {

          });
        },
        follow: function (parentId) {
          var data = {
            parentId: parentId
          }
          var app = this;
          this.post('/timizhuo/fans/follow', data, function (res) {
            if (res.data.code == '200') {
              app.isFollow = true;
              var toast = new auiToast();
              toast.success({
                title:"关注成功",
                duration:2000
              });
            } else {
              var toast = new auiToast();
              toast.fail({
                title:"登录后才能关注哦！",
                duration:2000
              });
            }
          }, function (err) {
          });
        }
      }
    }
</script>

<style scoped>

</style>
