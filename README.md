# project
겜색기

# 깃 초기설정. (다른 곳에서 할때) 
git config --global user.name "이름"  
git config --global user.email "이메일"  
git clone https://github.com/chlwlsgh777/project.git  

# 깃 사용법
cd 디렉토리명   * 디렉토리 위치 변경  
git remote update >> 갱신  
git checkout -b (브랜치명)  >> 브랜치 생성 & 이동 ( -b 빼면 해당 브랜치로 이동 )  
git merge <다른 브랜치이름> >> 브랜치 합치기 ( 현재 위치한 브랜치 기준으로 )  


# 작업한 내용 저장할때
git add .  
git commit -m "커밋할 내용(바꾼 내용)"  
git push --set-upstream origin 브랜치명 --> 로컬 브랜치 B를 원격 origin의 B 브랜치에 푸시하고, 두 브랜치 간의 추적 관계를 설정 
                                            이후에는 git push 만 써도됨.
git push >> 깃허브 레포지토리에 바꼇는지 확인하기~  




