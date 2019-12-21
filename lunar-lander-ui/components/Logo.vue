<template>
  <div class="frame">
    <div class="lander-body" >
    </div>
    <div class="feet" >
        <div class="foot f1" />
        <div class="foot f2" />
        <div class="foot f3" />
    </div>
  </div>
</template>

<style>
.frame{
  height:180px;
  width: 300px;
  animation: turn 2s linear forwards 1s;
  transform: rotateX(360deg);
}

.lander-body{
  width: 100%;
  position: relative;
  overflow: hidden;
  background-image: linear-gradient(to bottom, #211c17, #080707);
  background-size: 100% 100%;
  border-left: 50px solid transparent;
  border-right: 50px solid transparent;
  border-bottom: 100px solid gray;
  height:0
}
  

.feet{
  width: 100%;
  display: inline-block;
  position: relative;
  top: 0;
  left: 0;
  height: 0;
}

.foot{
  display: inline-block;
  height:0;
  width: 70px;
}

.foot.f1{
  animation: goleft 0.5s linear forwards 3.5s;
  border-bottom: 100px solid gray;
  border-left: 20px solid transparent;
  border-right: 25px solid transparent;
}

.foot.f2{
  border-left: 20px solid transparent;
  border-right: 20px solid transparent;
  border-bottom: 100px solid gray;
  margin-left: 2vw;
  margin-right: 2vw;
}

.foot.f3{
  animation: goright 0.5s linear forwards 3.5s;
  border-left: 25px solid transparent;
  border-right: 20px solid transparent;
  border-bottom: 100px solid gray;
}

@keyframes turn {
  100% {
    transform: rotateX(0deg);
  }
}

@keyframes godown {
  100% {
    top: 180px;
  }
}

@keyframes goright {
  100% {
    left: 70px;
  }
}
</style>
