
rm(list=ls(all=TRUE))
library(coin)
setwd("C:/hObbE/projects/coding/research/s4rdm3x/r-scripts")

source("functions/getFilteredData.R")
source("functions/loadData.R")


d_1 <- getFilteredData(0.1, 25, loadData("jabref_37_1/rand.csv"))
d_2 <- getFilteredData(0.1, 25, loadData("jabref_37_1/linecount.csv"))

d_1 <- getFilteredData(0.1, 25, loadData("lucene/rand.csv"))
d_2 <- getFilteredData(0.1, 25, loadData("lucene/linecount.csv"))




d_2$data$metric = "linecount_old"

nrow(d_1$data)
nrow(d_2$data)
summary(d_1$data)
summary(d_2$data)

data <- rbind(d_1$data, d_2$data)
summary(data)
boxplot(ap~metric, data=data)
boxplot(mr~metric, data=data)
boxplot(mp~metric, data=data)
boxplot(mappingPercent~metric, data=data)
boxplot(initialClustered~metric, data=data)
plot(ap~phi, data=d_1$data)
plot(ap~phi, data=d_2$data)


wilcox.test(d_1$data$ap, d_2$data$ap, conf.int=TRUE)
median(d_1$data$ap) - median(d_2$data$ap)
wilcox_test(data$ap~data$metric)

# compute effect size r
#https://stats.stackexchange.com/questions/133077/effect-size-to-wilcoxon-signed-rank-test
abs(statistic(wilcox_test(data$ap~data$metric)) / sqrt(nrow(data)))


# load all
all <- getFilteredData(0.001, 25, loadData("argouml_all.csv"))
all <- getFilteredData(0.001, 25, loadData("jabref_37_1/all.csv"))
all <- getFilteredData(0.0001, 25, loadData("teammates_all.csv"))
all <- getFilteredData(0.001, 25, loadData("ant_all.csv"))
all <- getFilteredData(0.001, 25, loadData("lucene/all.csv"))
all <- getFilteredData(0.001, 25, loadData("sweethome3d_all.csv"))


length(unique(all$data$metric))
nrow(all$data)
summary(all$data)
all$phi_min
all$phi_max

all$omega_min
all$omega_max

plot(ap~phi, data=all$data)
plot(ap~omega, data=all$data)

oldpar = par(mar=c(20,5,1,1))
boxplot(ap~metric, data=all$data, las=2)
boxplot(mr~metric, data=all$data, las=2)
boxplot(mp~metric, data=all$data, las=2)
boxplot(h_mam~metric, data=all$data, las=2)
boxplot(h_am~metric, data=all$data, las=2)

par(oldpar)
