import axiosInstance from "../utils/axiosInstance";
import { getCurrentUser } from "./authService";

const basicOrderUrl = "/order-module/api/v1/orders/"

export async function getAllOrdersByUser() {
    let username = await getCurrentUser()
    axiosInstance
        .get(basicOrderUrl + "all/user/" + username)
        .then((res) => {
            return res.data
        }).catch((err) => console.log(err))
}

export async function getOrderById(orderId: string) {
    axiosInstance
        .get(basicOrderUrl + orderId)
        .then((res) => {
            return res.data
        }).catch((err) => console.log(err))
}

export async function getActiveOrder() {
    let username = await getCurrentUser()
    axiosInstance
        .get(basicOrderUrl + "active/users/" + username)
        .then((res) => {
            return res.data
        }).catch((err) => console.log(err))
}

export async function updateOrder(orderId: string) {
    axiosInstance
        .put(basicOrderUrl + orderId)
        .then((res) => {
            return res.data
        }).catch((err) => console.log(err))
}

export async function makeOrder() {
    let username = await getCurrentUser()
    axiosInstance
        .put(basicOrderUrl + "users/" + username)
        .then((res) => {
            return res.data
        }).catch((err) => console.log(err))
}

export async function deleteOrder(orderId: string) {
    axiosInstance
        .delete(basicOrderUrl + orderId)
        .then((res) => {
            return res.data
        }).catch((err) => console.log(err))
}