
rm(list=ls(all=TRUE))
library(coin)
setwd("C:/hObbE/projects/coding/research/s4rdm3x/r-scripts")

source("functions/getFilteredData.R")
source("functions/loadData.R")

# comparison of individual metrics

d_1 <- getFilteredData(1.00, 250, loadData("2018-10-29_own_metrics/jabref_37_1/bccc.csv"))
d_2 <- getFilteredData(1.00, 250, loadData("180921/jabref_37_1/bccc.csv"))

d_1 <- getFilteredData(0.1, 25, loadData("lucene/rand.csv"))
d_2 <- getFilteredData(0.1, 25, loadData("lucene/linecount.csv"))


d_1$phi_min
d_1$phi_max
d_2$phi_min
d_2$phi_max

d_1$omega_min
d_1$omega_max
d_2$omega_min
d_2$omega_max



d_1$data$metric = "bccc_new"
d_2$data$metric = "bccc_old"

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
boxplot(h_mam~metric, data=data)
plot(h_mam~phi, data=d_1$data)
plot(h_mam~phi, data=d_2$data)


wilcox.test(d_1$data$ap, d_2$data$ap, conf.int=TRUE)
median(d_1$data$ap) - median(d_2$data$ap)
wilcox_test(data$ap~data$metric)

wilcox.test(d_1$data$h_mam, d_2$data$h_mam, conf.int=TRUE)
median(d_1$data$h_mam) - median(d_2$data$h_mam)
wilcox_test(data$h_mam~data$metric)

# compute effect size r
#https://stats.stackexchange.com/questions/133077/effect-size-to-wilcoxon-signed-rank-test
abs(statistic(wilcox_test(data$ap~data$metric)) / sqrt(nrow(data)))


# load all

source("functions/getFilteredData.R")
source("functions/loadData.R")


all <- getFilteredData(0.001, 25, loadData("180921/argouml/all.csv"))
oldall <- getFilteredData(0.1, 25, loadData("180921/jabref_37_1/all.csv"))
all <- getFilteredData(0.001, 25, loadData("180921/teammates/all.csv"))
all <- getFilteredData(0.001, 25, loadData("180921/ant/all.csv"))
all <- getFilteredData(0.001, 25, loadData("180921/lucene/all.csv"))
all <- getFilteredData(0.001, 25, loadData("180921/sweethome3d/all.csv"))

# icsa2019 own metrics
icsa2019_rand <- getFilteredData(0.1, 25, loadData("ICSA2019/jabref_37_1/rand.csv")) 
icsa2019_rand$phi_min
icsa2019_rand$phi_max
icsa2019_rand$omega_min
icsa2019_rand$omega_max

all_loaded <- loadData("ICSA2019/jabref_37_1/all.csv")
rand_loaded <- loadData("ICSA2019/jabref_37_1/rand.csv");

plot(omega~phi, data = rand_loaded)

rand_omegaphi <- getOmegaPhi(0.001, rand_loaded)
#all<- getFilteredData(0.1, 25, loadData("ICSA2019/jabref_37_1/all.csv"))
all <- getFilteredData_OmegaPhi(10, 25, rand_omegaphi, all_loaded)
metrics <- with(all$data, reorder(metric, -h_mam, FUN=median))
boxplot(h_mam~metrics, data=all$data, las=2)
metrics
nrow(all$data)
median(all$data$h_mam[all$data$metric == "Rank"])
rand_omegaphi$omega_min
rand_omegaphi$omega_max
rand_omegaphi$phi_min
rand_omegaphi$phi_max


compare_data <- all$data[all$data$metric == "rand" | all$data$metric == "fanout_linecount",]
nrow(compare_data)
wilcox_test(compare_data$h_mam~compare_data$metric)

statistic(wilcox_test(compare_data$h_mam~compare_data$metric))

abs(statistic(wilcox_test(compare_data$h_mam~compare_data$metric)) / sqrt(nrow(compare_data)))

median(compare_data$h_mam[compare_data$metric == "rand"]) - median(compare_data$h_mam[compare_data$metric == "fanout_linecount"])


hist(all$data$h_mam[all$data$metric == "fanout"])

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
metrics <- with(all$data, reorder(metric, -ap, FUN=median))
boxplot(ap~metrics, data=all$data, las=2)

metrics <- with(all$data, reorder(metric, -mr, FUN=median))
boxplot(mr~metrics, data=all$data, las=2)

metrics <- with(all$data, reorder(metric, -mp, FUN=median))
boxplot(mp~metrics, data=all$data, las=2)

metrics <- with(all$data, reorder(metric, -h_mam, FUN=median))
boxplot(h_mam~metrics, data=all$data, las=2)

metrics <- with(oldall$data, reorder(metric, -h_mam, FUN=median))
boxplot(h_mam~metrics, data=oldall$data, las=2)


boxplot(h_am~metric, data=all$data, las=2)

par(oldpar)
