
rm(list=ls(all=TRUE))
library(coin)
setwd("C:/hObbE/projects/coding/research/s4rdm3x/r-scripts")

source("functions/getFilteredData.R")
source("functions/loadData.R")


d_1 <- getFilteredData(0.1, loadData("jabref_rand.csv"))
d_2 <- getFilteredData(0.1, loadData("jabref_fanin.csv"))

summary(d_1$data)
summary(d_2$data)

data <- rbind(d_1$data, d_2$data)
summary(data)
boxplot(ap~metric, data=data)


wilcox.test(d_1$data$ap, d_2$data$ap, conf.int=TRUE)
median(d_1$data$ap) - median(d_2$data$ap)
wilcox_test(data$ap~data$metric)

# compute effect size r
#https://stats.stackexchange.com/questions/133077/effect-size-to-wilcoxon-signed-rank-test
abs(statistic(wilcox_test(data$ap~data$metric)) / sqrt(nrow(data)))